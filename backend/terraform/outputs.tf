output "instance_id" {
  description = "EC2 instance ID."
  value       = aws_instance.app.id
}

output "instance_public_ip" {
  description = "Public IP (Elastic IP if allocated, otherwise EC2 public IP)."
  value       = try(aws_eip.app[0].public_ip, aws_instance.app.public_ip)
}

output "instance_public_dns" {
  description = "Public DNS name of the EC2 instance."
  value       = aws_instance.app.public_dns
}

output "ecr_repository_name" {
  description = "ECR repository name."
  value       = aws_ecr_repository.app.name
}

output "ecr_repository_url" {
  description = "ECR repository URL."
  value       = aws_ecr_repository.app.repository_url
}

output "github_actions_role_arn" {
  description = "IAM role ARN for GitHub Actions OIDC deploy workflow."
  value       = aws_iam_role.github_actions.arn
}

output "deploy_command_example" {
  description = "Remote deploy command used by SSM."
  value       = "sudo /usr/local/bin/hotdeal-deploy <image-uri>"
}

output "postgres_password" {
  description = "Resolved PostgreSQL password."
  value       = local.resolved_postgres_password
  sensitive   = true
}

output "redis_password" {
  description = "Resolved Redis password."
  value       = local.resolved_redis_password
  sensitive   = true
}
