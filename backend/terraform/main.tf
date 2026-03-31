data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

data "aws_caller_identity" "current" {}

data "aws_partition" "current" {}

locals {
  name_prefix = "${var.project_name}-${var.environment}"

  default_subnet_ids = sort(data.aws_subnets.default.ids)
  selected_subnet_id = coalesce(var.subnet_id, local.default_subnet_ids[0])

  ami_ssm_parameter_name = var.ami_architecture == "arm64" ? "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-arm64" : "/aws/service/ami-amazon-linux-latest/al2023-ami-kernel-6.1-x86_64"

  ecr_registry = "${data.aws_caller_identity.current.account_id}.dkr.ecr.${var.aws_region}.amazonaws.com"
  app_image    = "${local.ecr_registry}/${aws_ecr_repository.app.name}:${var.initial_image_tag}"

  resolved_postgres_password = coalesce(var.postgres_password, random_password.postgres.result)
  resolved_redis_password    = coalesce(var.redis_password, random_password.redis.result)

  github_oidc_provider_arn = var.create_github_oidc_provider ? aws_iam_openid_connect_provider.github[0].arn : var.existing_github_oidc_provider_arn

  common_tags = merge(
    {
      Project     = var.project_name
      Environment = var.environment
      ManagedBy   = "terraform"
    },
    var.tags
  )
}

data "aws_ssm_parameter" "al2023_ami" {
  name = local.ami_ssm_parameter_name
}

resource "random_password" "postgres" {
  length  = 24
  special = false
}

resource "random_password" "redis" {
  length  = 24
  special = false
}

resource "aws_ecr_repository" "app" {
  name                 = var.ecr_repository_name
  image_tag_mutability = "MUTABLE"

  image_scanning_configuration {
    scan_on_push = true
  }

  force_delete = true

  tags = merge(
    local.common_tags,
    { Name = "${local.name_prefix}-ecr" }
  )
}

resource "aws_ecr_lifecycle_policy" "app" {
  repository = aws_ecr_repository.app.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep recent images only"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = 30
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}

resource "aws_security_group" "app" {
  name        = "${local.name_prefix}-sg"
  description = "Security group for ${local.name_prefix} application EC2"
  vpc_id      = data.aws_vpc.default.id

  dynamic "ingress" {
    for_each = toset(var.web_ingress_ports)

    content {
      description = "Web traffic on port ${ingress.value}"
      from_port   = ingress.value
      to_port     = ingress.value
      protocol    = "tcp"
      cidr_blocks = var.web_ingress_cidrs
    }
  }

  dynamic "ingress" {
    for_each = length(var.allowed_ssh_cidrs) > 0 ? [1] : []

    content {
      description = "SSH access"
      from_port   = 22
      to_port     = 22
      protocol    = "tcp"
      cidr_blocks = var.allowed_ssh_cidrs
    }
  }

  egress {
    description      = "Allow all outbound traffic"
    from_port        = 0
    to_port          = 0
    protocol         = "-1"
    cidr_blocks      = ["0.0.0.0/0"]
    ipv6_cidr_blocks = ["::/0"]
  }

  tags = merge(
    local.common_tags,
    { Name = "${local.name_prefix}-sg" }
  )
}

resource "aws_iam_role" "ec2" {
  name = "${local.name_prefix}-ec2-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Service = "ec2.amazonaws.com"
        }
        Action = "sts:AssumeRole"
      }
    ]
  })

  tags = local.common_tags
}

resource "aws_iam_role_policy_attachment" "ec2_ssm_core" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonSSMManagedInstanceCore"
}

resource "aws_iam_role_policy_attachment" "ec2_ecr_read_only" {
  role       = aws_iam_role.ec2.name
  policy_arn = "arn:${data.aws_partition.current.partition}:iam::aws:policy/AmazonEC2ContainerRegistryReadOnly"
}

resource "aws_iam_instance_profile" "ec2" {
  name = "${local.name_prefix}-ec2-instance-profile"
  role = aws_iam_role.ec2.name
}

resource "aws_instance" "app" {
  ami           = data.aws_ssm_parameter.al2023_ami.value
  instance_type = var.instance_type

  subnet_id                   = local.selected_subnet_id
  vpc_security_group_ids      = [aws_security_group.app.id]
  associate_public_ip_address = true

  key_name             = var.key_name
  iam_instance_profile = aws_iam_instance_profile.ec2.name

  user_data = templatefile("${path.module}/user_data.sh.tftpl", {
    aws_region                   = var.aws_region
    deploy_base_dir              = var.deploy_base_dir
    repo_url                     = var.repo_url
    repo_branch                  = var.repo_branch
    repo_subdir                  = var.repo_subdir
    ecr_registry                 = local.ecr_registry
    app_image                    = local.app_image
    postgres_db                  = var.postgres_db
    postgres_user                = var.postgres_user
    postgres_password            = local.resolved_postgres_password
    redis_password               = local.resolved_redis_password
    redis_database               = tostring(var.redis_database)
    jpa_ddl_auto                 = var.jpa_ddl_auto
    crawler_request_delay_min_ms = tostring(var.crawler_request_delay_min_ms)
    crawler_request_delay_max_ms = tostring(var.crawler_request_delay_max_ms)
  })

  metadata_options {
    http_endpoint = "enabled"
    http_tokens   = "required"
  }

  root_block_device {
    volume_size           = var.root_volume_size_gb
    volume_type           = var.root_volume_type
    encrypted             = true
    delete_on_termination = true
  }

  tags = merge(
    local.common_tags,
    { Name = "${local.name_prefix}-ec2" }
  )
}

resource "aws_eip" "app" {
  count = var.allocate_eip ? 1 : 0

  domain   = "vpc"
  instance = aws_instance.app.id

  tags = merge(
    local.common_tags,
    { Name = "${local.name_prefix}-eip" }
  )
}

resource "aws_iam_openid_connect_provider" "github" {
  count = var.create_github_oidc_provider ? 1 : 0

  url             = "https://token.actions.githubusercontent.com"
  client_id_list  = ["sts.amazonaws.com"]
  thumbprint_list = ["6938fd4d98bab03faadb97b34396831e3780aea1"]

  tags = local.common_tags
}

resource "aws_iam_role" "github_actions" {
  name = var.github_actions_role_name

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Principal = {
          Federated = local.github_oidc_provider_arn
        }
        Action = "sts:AssumeRoleWithWebIdentity"
        Condition = {
          StringEquals = {
            "token.actions.githubusercontent.com:aud" = "sts.amazonaws.com"
          }
          StringLike = {
            "token.actions.githubusercontent.com:sub" = "repo:${var.github_owner}/${var.github_repo}:ref:refs/heads/${var.github_branch}"
          }
        }
      }
    ]
  })

  lifecycle {
    precondition {
      condition     = var.create_github_oidc_provider || var.existing_github_oidc_provider_arn != null
      error_message = "When create_github_oidc_provider is false, existing_github_oidc_provider_arn must be set."
    }
  }

  tags = local.common_tags
}

resource "aws_iam_role_policy" "github_actions_deploy" {
  name = "${local.name_prefix}-github-actions-deploy"
  role = aws_iam_role.github_actions.id

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Sid    = "EcrAuth"
        Effect = "Allow"
        Action = [
          "ecr:GetAuthorizationToken"
        ]
        Resource = "*"
      },
      {
        Sid    = "EcrPushPullRepository"
        Effect = "Allow"
        Action = [
          "ecr:BatchCheckLayerAvailability",
          "ecr:CompleteLayerUpload",
          "ecr:InitiateLayerUpload",
          "ecr:UploadLayerPart",
          "ecr:PutImage",
          "ecr:BatchGetImage",
          "ecr:DescribeRepositories"
        ]
        Resource = aws_ecr_repository.app.arn
      },
      {
        Sid    = "SsmRemoteDeploy"
        Effect = "Allow"
        Action = [
          "ssm:SendCommand"
        ]
        Resource = [
          "arn:${data.aws_partition.current.partition}:ssm:${var.aws_region}::document/AWS-RunShellScript",
          "arn:${data.aws_partition.current.partition}:ec2:${var.aws_region}:${data.aws_caller_identity.current.account_id}:instance/${aws_instance.app.id}"
        ]
      },
      {
        Sid    = "SsmReadCommandResult"
        Effect = "Allow"
        Action = [
          "ssm:GetCommandInvocation",
          "ssm:ListCommandInvocations",
          "ssm:ListCommands"
        ]
        Resource = "*"
      },
      {
        Sid    = "DescribeEc2"
        Effect = "Allow"
        Action = [
          "ec2:DescribeInstances"
        ]
        Resource = "*"
      }
    ]
  })
}
