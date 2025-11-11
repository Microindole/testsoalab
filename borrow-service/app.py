from flask import Flask, request, jsonify
import requests # 引入 requests
import os

app = Flask(__name__)

# 1. 定义 Java 数据服务的根 URL
JAVA_API_URL = "http://localhost:8080"

# 2. (保留) 根路径
@app.route("/")
def hello():
    return "Hello from Python Borrow Service (Port 8081) - Now connected to Java DB"

# 3. API: 获取所有借用记录 (代理 Java 服务)
@app.route("/borrows", methods=["GET"])
def get_borrows():
    try:
        # 跨语言调用：GET 到 Java 的 /borrows
        response = requests.get(f"{JAVA_API_URL}/borrows")
        response.raise_for_status() # 如果是 4xx 或 5xx，将引发异常
        return jsonify(response.json()), response.status_code
    except requests.exceptions.RequestException as e:
        return jsonify({"error": str(e)}), 500

# 4. API: 创建新借用记录 (代理 Java 服务)
@app.route("/borrows", methods=["POST"])
def create_borrow():
    client_data = request.json # 这是从 Java 网关传来的数据
    try:
        # 跨语言调用：POST 到 Java 的 /borrows
        response = requests.post(f"{JAVA_API_URL}/borrows", json=client_data)
        response.raise_for_status() # 引发异常

        # 将 Java 服务返回的新记录 (包含 ID) 转发回去
        return jsonify(response.json()), response.status_code
    except requests.exceptions.RequestException as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081, debug=True)