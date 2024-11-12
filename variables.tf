variable "resource_prefix" {
  type        = string
  description = "Prefix for resource names"
  default     = "terraform-hw"
}

variable "ssh_public_key" {
  type        = string
  description = "Public SSH key for virtual machine access"
  default     = ""
}
