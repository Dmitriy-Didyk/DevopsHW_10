output "vm_public_ips" {
  value = azurerm_public_ip.vm_public_ip[*].ip_address
}
