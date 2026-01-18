#!/bin/bash
# Script helper để format JDBC connection string từ Render database variables
# Sử dụng khi cần set DBMS_CONNECTION thủ công trong Render Dashboard

# Nếu dùng PostgreSQL trên Render:
# jdbc:postgresql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?sslmode=require

# Nếu dùng MySQL (external hoặc Render MySQL nếu có):
# jdbc:mysql://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true

echo "JDBC URL Format Helper"
echo "======================"
echo ""
echo "PostgreSQL (Render default):"
echo "jdbc:postgresql://\${DATABASE_HOST}:\${DATABASE_PORT}/\${DATABASE_NAME}?sslmode=require"
echo ""
echo "MySQL (External service):"
echo "jdbc:mysql://\${DATABASE_HOST}:\${DATABASE_PORT}/\${DATABASE_NAME}?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
echo ""
echo "MySQL (với specific host):"
echo "jdbc:mysql://<your-mysql-host>:3306/coivietdb?useSSL=true&serverTimezone=Asia/Ho_Chi_Minh&allowPublicKeyRetrieval=true"
echo ""
echo "Lưu ý: Thay <your-mysql-host> bằng hostname thực tế của MySQL service"
