# Создание ресурсной группы
resource "azurerm_resource_group" "rg" {
  name     = "${var.resource_prefix}-rg"
  location = "Canada Central"
}

# Создание виртуальной сети
resource "azurerm_virtual_network" "vnet" {
  name                = "${var.resource_prefix}-vnet"
  address_space       = ["10.0.0.0/16"]
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}

# Создание подсети
resource "azurerm_subnet" "subnet" {
  name                 = "${var.resource_prefix}-subnet"
  resource_group_name  = azurerm_resource_group.rg.name
  virtual_network_name = azurerm_virtual_network.vnet.name
  address_prefixes     = ["10.0.1.0/24"]
}

# Создание группы безопасности сети
resource "azurerm_network_security_group" "nsg" {
  name                = "${var.resource_prefix}-nsg"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
}

# Правило для доступа по SSH
resource "azurerm_network_security_rule" "ssh_rule" {
  name                        = "${var.resource_prefix}-Allow-SSH"
  priority                    = 1001
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "22"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  resource_group_name         = azurerm_resource_group.rg.name
  network_security_group_name = azurerm_network_security_group.nsg.name
}

# Правило для доступа по HTTP
resource "azurerm_network_security_rule" "http_rule" {
  name                        = "${var.resource_prefix}-Allow-HTTP"
  priority                    = 1002
  direction                   = "Inbound"
  access                      = "Allow"
  protocol                    = "Tcp"
  source_port_range           = "*"
  destination_port_range      = "80"
  source_address_prefix       = "*"
  destination_address_prefix  = "*"
  resource_group_name         = azurerm_resource_group.rg.name
  network_security_group_name = azurerm_network_security_group.nsg.name
}

# Создание публичного IP для виртуальных машин
resource "azurerm_public_ip" "vm_public_ip" {
  count               = 2
  name                = "${var.resource_prefix}-vm-public-ip-${count.index}"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  allocation_method   = "Static"
  sku                 = "Standard"
}

resource "azurerm_public_ip" "lb_public_ip" {
  name                = "${var.resource_prefix}-lb-public-ip"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  allocation_method   = "Static"
  sku                 = "Standard"
}

# Создание Load Balancer с фронтенд IP и Backend Pool
resource "azurerm_lb" "lb" {
  name                = "${var.resource_prefix}-lb"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  sku                 = "Standard"

  frontend_ip_configuration {
    name                 = "frontend"
    public_ip_address_id = azurerm_public_ip.lb_public_ip.id
  }
}

# Backend адрес-пул для Load Balancer
resource "azurerm_lb_backend_address_pool" "backend_pool" {
  name            = "${var.resource_prefix}-backend-pool"
  loadbalancer_id = azurerm_lb.lb.id
}

# Создание сетевых интерфейсов с публичными IP-адресами
resource "azurerm_network_interface" "vm_nic" {
  count               = 2
  name                = "${var.resource_prefix}-nic-${count.index}"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name

  ip_configuration {
    name                          = "internal"
    subnet_id                     = azurerm_subnet.subnet.id
    private_ip_address_allocation = "Dynamic"
    public_ip_address_id          = azurerm_public_ip.vm_public_ip[count.index].id
  }
}

# Ассоциация сетевых интерфейсов с backend-пулом Load Balancer
resource "azurerm_network_interface_backend_address_pool_association" "nic_lb_association" {
  count                    = 2
  network_interface_id     = azurerm_network_interface.vm_nic[count.index].id
  backend_address_pool_id  = azurerm_lb_backend_address_pool.backend_pool.id
  ip_configuration_name    = "internal"
}

# Создание виртуальных машин
resource "azurerm_linux_virtual_machine" "vm" {
  count               = 2
  name                = "${var.resource_prefix}-vm-${count.index}"
  location            = azurerm_resource_group.rg.location
  resource_group_name = azurerm_resource_group.rg.name
  network_interface_ids = [azurerm_network_interface.vm_nic[count.index].id]
  size                = "Standard_B1s"

  admin_username      = "azureadmin"
  admin_password      = "P@ssw0rd123!"
  disable_password_authentication = false

  admin_ssh_key {
    username   = "azureadmin"
    public_key = var.ssh_public_key
  }

  os_disk {
    caching              = "ReadWrite"
    storage_account_type = "Standard_LRS"
    disk_size_gb         = 30
  }

  source_image_reference {
    publisher = "Canonical"
    offer     = "UbuntuServer"
    sku       = "18.04-LTS"
    version   = "latest"
  }
}

resource "azurerm_subnet_network_security_group_association" "subnet_nsg_association" {
  subnet_id                 = azurerm_subnet.subnet.id
  network_security_group_id = azurerm_network_security_group.nsg.id
}

resource "azurerm_network_interface_security_group_association" "nic_nsg_association" {
  count                    = 2
  network_interface_id     = azurerm_network_interface.vm_nic[count.index].id
  network_security_group_id = azurerm_network_security_group.nsg.id
}
