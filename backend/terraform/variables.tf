variable "aws_region" {
  description = "AWS region to deploy resources into."
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "Project name used for resource naming and tags."
  type        = string
  default     = "hotdeal"
}

variable "environment" {
  description = "Environment name used for resource naming and tags."
  type        = string
  default     = "prod"
}

variable "instance_type" {
  description = "EC2 instance type for the application server."
  type        = string
  default     = "t4g.micro"
}

variable "ami_architecture" {
  description = "AMI architecture for Amazon Linux 2023."
  type        = string
  default     = "arm64"

  validation {
    condition     = contains(["arm64", "x86_64"], var.ami_architecture)
    error_message = "ami_architecture must be one of: arm64, x86_64."
  }
}

variable "key_name" {
  description = "Optional EC2 key pair name for SSH access."
  type        = string
  default     = null
}

variable "subnet_id" {
  description = "Optional subnet ID. If null, the first default subnet is used."
  type        = string
  default     = null
}

variable "allowed_ssh_cidrs" {
  description = "CIDR blocks allowed to SSH (port 22). Empty list disables SSH ingress."
  type        = list(string)
  default     = []
}

variable "web_ingress_ports" {
  description = "Public ingress ports for web traffic."
  type        = list(number)
  default     = [80, 443]
}

variable "web_ingress_cidrs" {
  description = "CIDR blocks allowed for web ingress ports."
  type        = list(string)
  default     = ["0.0.0.0/0"]
}

variable "allocate_eip" {
  description = "Allocate and attach an Elastic IP (stable public IP)."
  type        = bool
  default     = false
}

variable "root_volume_size_gb" {
  description = "Root EBS volume size in GB."
  type        = number
  default     = 20
}

variable "root_volume_type" {
  description = "Root EBS volume type."
  type        = string
  default     = "gp3"
}

variable "repo_url" {
  description = "Git repository URL cloned on instance boot."
  type        = string
}

variable "repo_branch" {
  description = "Git branch checked out by the deploy helper script."
  type        = string
  default     = "main"
}

variable "repo_subdir" {
  description = "Application subdirectory inside the repository."
  type        = string
  default     = "backend"
}

variable "deploy_base_dir" {
  description = "Base directory used on the EC2 instance for deployment artifacts."
  type        = string
  default     = "/opt/hotdeal"
}

variable "ecr_repository_name" {
  description = "ECR repository name for backend images."
  type        = string
  default     = "hotdeal-backend"
}

variable "initial_image_tag" {
  description = "Initial image tag to write into env file before first GitHub Actions deploy."
  type        = string
  default     = "latest"
}

variable "postgres_db" {
  description = "PostgreSQL database name used by docker-compose."
  type        = string
  default     = "hotdeal"
}

variable "postgres_user" {
  description = "PostgreSQL username used by docker-compose."
  type        = string
  default     = "hotdeal"
}

variable "postgres_password" {
  description = "Optional PostgreSQL password. If null, Terraform generates one."
  type        = string
  sensitive   = true
  default     = null
}

variable "redis_password" {
  description = "Optional Redis password. If null, Terraform generates one."
  type        = string
  sensitive   = true
  default     = null
}

variable "redis_database" {
  description = "Redis database index."
  type        = number
  default     = 0
}

variable "jpa_ddl_auto" {
  description = "Spring JPA ddl-auto value for production."
  type        = string
  default     = "update"
}

variable "github_owner" {
  description = "GitHub owner (user or org) for OIDC trust policy."
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name for OIDC trust policy."
  type        = string
}

variable "github_branch" {
  description = "GitHub branch allowed to assume OIDC deploy role."
  type        = string
  default     = "main"
}

variable "create_github_oidc_provider" {
  description = "Create IAM OIDC provider for token.actions.githubusercontent.com."
  type        = bool
  default     = true
}

variable "existing_github_oidc_provider_arn" {
  description = "Existing GitHub OIDC provider ARN when create_github_oidc_provider is false."
  type        = string
  default     = null
}

variable "github_actions_role_name" {
  description = "IAM role name assumed by GitHub Actions."
  type        = string
  default     = "hotdeal-github-actions-deploy-role"
}

variable "tags" {
  description = "Additional tags applied to resources."
  type        = map(string)
  default     = {}
}
