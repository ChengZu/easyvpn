# 西瓜vpn

# 如何配置服务器（Ubuntu）

1.安装jdk
```  
apt install openjdk-11-jdk-headless
```  
2.运行程序
 
 将build/XiGuaVpn.jar 拷贝到服务器(用WinSCP), 执行下面命令运行
```  
 java -jar XiGuaVpn.jar 80
```  

# 如何安装客户端

1.将build/XiGuaVpn.apk 拷贝到android手机上安装

2.ip选项填服务器IP地址, port填服务器端口

3.点击启动
