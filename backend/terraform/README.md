# Terraform: EC2 + ECR + GitHub Actions OIDC Auto Deploy

This stack provisions:

- EC2 instance (default `t4g.micro`) in `ap-northeast-2`
- Security group (+ optional SSH ingress)
- Optional Elastic IP
- ECR repository for backend images
- EC2 IAM role (SSM + ECR pull)
- GitHub OIDC provider/role for CI/CD deploy
- `user_data` that installs Docker and creates `/usr/local/bin/hotdeal-deploy`

## 1. Prerequisites

- Terraform `>= 1.6`
- AWS CLI configured (`aws configure`)
- A GitHub repository containing this backend

## 2. Configure variables

```bash
cd terraform
cp terraform.tfvars.example terraform.tfvars
```

Edit `terraform.tfvars`:

- `repo_url` (required)
- `repo_subdir` (`backend` for this repo)
- `github_owner`, `github_repo` (required)
- `key_name` and `allowed_ssh_cidrs` if SSH is needed
- `crawler_request_delay_min_ms`, `crawler_request_delay_max_ms` for crawler random delay range
- If OIDC provider already exists in your account, set:
  - `create_github_oidc_provider = false`
  - `existing_github_oidc_provider_arn = "arn:...:oidc-provider/token.actions.githubusercontent.com"`

## 3. Apply Terraform

```bash
terraform init
terraform plan
terraform apply
```

Save these outputs:

- `instance_id`
- `ecr_repository_name`
- `github_actions_role_arn`

## 4. GitHub repository settings

In GitHub repo settings:

- `Secrets and variables` -> `Actions` -> `Secrets`:
  - `AWS_ROLE_ARN` = `github_actions_role_arn` output
- `Secrets and variables` -> `Actions` -> `Variables`:
  - `AWS_REGION` = `ap-northeast-2`
  - `ECR_REPOSITORY` = `ecr_repository_name` output
  - `EC2_INSTANCE_ID` = `instance_id` output

## 5. Deploy flow

- Push to `main` branch
- GitHub Actions builds Docker image and pushes to ECR
- GitHub Actions calls SSM command on EC2
- EC2 runs `/usr/local/bin/hotdeal-deploy <image-uri>`

## 6. Notes

- `deploy/docker-compose.prod.yml` uses `APP_IMAGE` from `deploy/.env`.
- Terraform creates `/etc/hotdeal/app.env` and copies it to `deploy/.env`.
- `repo_url` must be cloneable from EC2 (`public repo` or HTTPS URL with token).
- `terraform destroy` removes infra including ECR images (`force_delete = true`).
