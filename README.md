
# 如何配置服务器（Ubuntu）

1.安装jdk

 apt-get install openjdk-8-jdk

2.运行程序
 
 将build/EasyVpn.jar 拷贝到服务器,执行下面命令运行

 java -jar EasyVpn.jar


断开ssh后仍运行，可使用screen

 安装screen
 
  apt-get install screen
 
 启动srceen
 
  screen -S vpn
 
 运行vpn
 
  java -jar EasyVpn.jar
 

#screen -r vpn

#screen -X -S vpn quit

#这样退出的话，以后还可以通过screen -r （name）再次进入，快捷键命令：先同时按Ctrl+A+D键



# 如何安装客户端

1.将build/app-release.apk 拷贝到android手机上安装

2.ip选项填服务器IP地址,其他选项默认即可

3.点击启动
