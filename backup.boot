firewall {
    name DMZ-to-LAN {
        default-action drop
        enable-default-log
        rule 10 {
            action accept
            description "Allow UDP from DMZ to LAN"
            destination {
                address 172.16.200.10
                port 1514
            }
            protocol udp
        }
        rule 20 {
            action accept
            description "Allows traffic back through DMZ to LAN"
            state {
                established enable
                related enable
            }
        }
    }
    name DMZ-to-WAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
            }
        }
        rule 10 {
            action accept
            description "allow port 123 from web01"
            destination {
                port 123
            }
            protocol udp
            source {
                address 172.16.50.3
            }
        }
        rule 40 {
            action accept
            description "Accept traffic from jump"
            source {
                address 172.16.50.5
            }
        }
    }
    name LAN-to-DMZ {
        default-action drop
        enable-default-log
        rule 10 {
            action accept
            description "Allow TCP from LAN to DMZ"
            destination {
                address 172.16.50.3
                port 80
            }
            protocol tcp
        }
        rule 15 {
            action accept
            description "Allow lan access to nginx1"
            destination {
                address 172.16.50.5
                port 80
            }
            protocol tcp
        }
        rule 20 {
            action accept
            description "Allow MGMT to DMZ"
            destination {
                address 172.16.50.1-172.16.50.6
                port 22
            }
            protocol tcp
            source {
                address 172.16.200.11
            }
        }
        rule 30 {
            action accept
            description "Allow icmp from nginx to log01"
            destination {
                address 172.16.50.5
            }
            protocol icmp
            source {
                address 172.16.200.10
            }
        }
    }
    name LAN-to-WAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
        }
    }
    name WAN-to-DMZ {
        default-action drop
        enable-default-log
        rule 10 {
            action accept
            description "Allow WAN Access to Web01 HTTP"
            destination {
                address 172.16.50.3
                port 80
            }
            protocol tcp
        }
        rule 15 {
            action accept
            state {
                established enable
            }
        }
        rule 20 {
            action accept
            description "Allow SSH into Jump"
            destination {
                address 172.16.50.4
                port 22
            }
            protocol tcp
        }
    }
    name WAN-to-LAN {
        default-action drop
        enable-default-log
        rule 1 {
            action accept
            state {
                established enable
                related enable
            }
        }
    }
}
interfaces {
    ethernet eth0 {
        address 10.0.17.143/24
        description SEC350-WAN
        hw-id 00:50:56:b3:17:a5
    }
    ethernet eth1 {
        address 172.16.50.2/29
        description JOHN-DMZ
        hw-id 00:50:56:b3:6b:96
    }
    ethernet eth2 {
        address 172.16.150.2/24
        description JOHN-LAN
        hw-id 00:50:56:b3:1b:e5
    }
    loopback lo {
    }
}
nat {
    destination {
        rule 10 {
            destination {
                port 80
            }
            inbound-interface eth0
            protocol tcp
            translation {
                address 172.16.50.3
                port 80
            }
        }
        rule 20 {
            destination {
                port 22
            }
            inbound-interface eth0
            protocol tcp
            translation {
                address 172.16.50.4
                port 22
            }
        }
    }
    source {
        rule 10 {
            description "NAT FROM DMZ 2 WAN"
            outbound-interface eth0
            source {
                address 172.16.50.0/29
            }
            translation {
                address masquerade
            }
        }
        rule 15 {
            description "For LAN"
            outbound-interface eth0
            source {
                address 172.16.150.0/24
            }
            translation {
                address masquerade
            }
        }
        rule 20 {
            description "NAT FROM MGMT 2 WAN"
            outbound-interface eth0
            source {
                address 172.16.200.0/28
            }
            translation {
                address masquerade
            }
        }
    }
}
protocols {
    rip {
        interface eth2 {
        }
        network 172.16.50.0/29
    }
    static {
        route 0.0.0.0/0 {
            next-hop 10.0.17.2 {
            }
        }
    }
}
service {
    dns {
        forwarding {
            allow-from 172.16.50.0/29
            allow-from 172.16.150.0/24
            listen-address 172.16.50.2
            listen-address 172.16.150.2
            system
        }
    }
    ssh {
        listen-address 0.0.0.0
        port 22
    }
}
system {
    config-management {
        commit-revisions 100
    }
    conntrack {
        modules {
            ftp
            h323
            nfs
            pptp
            sip
            sqlnet
            tftp
        }
    }
    console {
        device ttyS0 {
            speed 115200
        }
    }
    host-name fw1-jtiseo
    login {
        user jtiseo {
            authentication {
                encrypted-password $6$k6/jPM9vTNmowhWW$GmkDIbINMjdxvxn1S.cI5dESRJ8HxTOrNnF8AX.4RF.tcqVWZVZbwKn9tkPg7sDOIuDb68DwqF0Jzv.wz0jua/
            }
            full-name "John Tiseo"
        }
        user vyos {
            authentication {
                encrypted-password $6$W6YAxeiEhLr/y.Qj$rimCMQFXFx0lRYVxqtNkLzSW5Jeha1MU1W3T0sgd9um9mX5LKUzDa9cQlmLI1Ppes8VcrIxK0qBu3dAQHZsQC/
            }
        }
    }
    name-server 8.8.8.8
    ntp {
        server time1.vyos.net {
        }
        server time2.vyos.net {
        }
        server time3.vyos.net {
        }
    }
    syslog {
        global {
            facility all {
                level info
            }
            facility protocols {
                level debug
            }
        }
        host 172.16.50.5 {
            facility authpriv {
                level info
            }
        }
        host 172.16.200.10 {
            facility kern {
                level debug
            }
            format {
                octet-counted
            }
            port 1514
        }
    }
}
zone-policy {
    zone DMZ {
        from LAN {
            firewall {
                name LAN-to-DMZ
            }
        }
        from WAN {
            firewall {
                name WAN-to-DMZ
            }
        }
        interface eth1
    }
    zone LAN {
        from DMZ {
            firewall {
                name DMZ-to-LAN
            }
        }
        from WAN {
            firewall {
                name WAN-to-LAN
            }
        }
        interface eth2
    }
    zone WAN {
        from DMZ {
            firewall {
                name DMZ-to-WAN
            }
        }
        from LAN {
            firewall {
                name LAN-to-WAN
            }
        }
        interface eth0
    }
}


// Warning: Do not remove the following line.
// vyos-config-version: "bgp@2:broadcast-relay@1:cluster@1:config-management@1:conntrack@3:conntrack-sync@2:dhcp-relay@2:dhcp-server@6:dhcpv6-server@1:dns-forwarding@3:firewall@7:flow-accounting@1:https@3:interfaces@25:ipoe-server@1:ipsec@8:isis@1:l2tp@4:lldp@1:mdns@1:nat@5:nat66@1:ntp@1:openconnect@1:ospf@1:policy@2:pppoe-server@5:pptp@2:qos@1:quagga@9:rpki@1:salt@1:snmp@2:ssh@2:sstp@4:system@22:vrf@3:vrrp@3:vyos-accel-ppp@2:wanloadbalance@3:webproxy@2"
// Release version: 1.4-rolling-202202030910
