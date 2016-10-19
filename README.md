# EncryptedDns4J-Server
 DNS防污染服务端，配合客户端EncryptedDns4J-Client实现DNS的无污染解析。  
  
# Requirement
Linux  
JDK1.8+  
Maven 3.3+  
unzip  
# Usage:
In your VPS ：  

git clone https://github.com/snail007/EncryptedDns4J-Server.git  

cd EncryptedDns4J-Server  

mvn install  

cp target/EncryptedDns4J-Server-1.0-SNAPSHOT-package.zip /root/  

cd /root/  

unzip EncryptedDns4J-Server-1.0-SNAPSHOT-package.zip  

cd EncryptedDns4J-Server-1.0-SNAPSHOT  

java -jar EncryptedDns4J-Server-1.0-SNAPSHOT-jar-with-dependencies.jar development  

# Notice

"development" is config subfolder's name,local it in config/development  

you can use "production" or "testing" or "development" for different environment.  

#Configuration

;监听IP,所有IP使用：0.0.0.0  
listen_ip=0.0.0.0  
;监听端口  
listen_port=10888  
;通讯加密数据的加密key  
encrypt_key=xxxxxxxxxxxxxx  