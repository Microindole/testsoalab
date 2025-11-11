from flask import Flask, request, jsonify
import mysql.connector
from datetime import datetime
import os

app = Flask(__name__)

# 1. 数据库配置 (使用你 application.properties 中的凭据)
DB_CONFIG = {
    'host': 'localhost',
    'port': 3306,
    'user': 'root',
    'password': 'abcABC369258',
    'database': 'soa_lab1'
}

def get_db_connection():
    """获取数据库连接"""
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        return conn
    except mysql.connector.Error as err:
        print(f"Error connecting to DB: {err}")
        return None

# 2. (保留) 根路径
@app.route("/")
def hello():
    return "Hello from Python Borrow Service (Port 8081) - Now connected to Java DB"

# 3. API: 获取所有借用记录 (实现)
@app.route("/borrows", methods=["GET"])
def get_borrows():
    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        if conn is None:
            return jsonify({"error": "Database connection failed"}), 500

        # dictionary=True 让结果返回为 { "column": "value" } 格式
        cursor = conn.cursor(dictionary=True)
        cursor.execute("SELECT * FROM borrow_records")
        records = cursor.fetchall()

        # Flask 无法直接序列化 datetime, 需要转换
        for record in records:
            if 'borrow_date' in record and record['borrow_date']:
                record['borrow_date'] = record['borrow_date'].isoformat()
            if 'return_date' in record and record['return_date']:
                record['return_date'] = record['return_date'].isoformat()

        return jsonify(records), 200

    except mysql.connector.Error as err:
        return jsonify({"error": str(err)}), 500
    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

# 4. API: 创建新借用记录 (实现)
# 供 Java 网关调用
@app.route("/borrows", methods=["POST"])
def create_borrow():
    client_data = request.json
    user_id = client_data.get('userId')
    material_id = client_data.get('materialId')

    if not user_id or not material_id:
        return jsonify({"error": "Missing userId or materialId"}), 400

    conn = None
    cursor = None
    try:
        conn = get_db_connection()
        if conn is None:
            return jsonify({"error": "Database connection failed"}), 500

        cursor = conn.cursor(dictionary=True)

        borrow_date = datetime.now()
        status = "BORROWED"

        sql = """
        INSERT INTO borrow_records (user_id, material_id, borrow_date, status)
        VALUES (%s, %s, %s, %s)
        """
        cursor.execute(sql, (user_id, material_id, borrow_date, status))
        conn.commit()

        # 获取新插入记录的 ID
        new_id = cursor.lastrowid

        # 返回新创建的完整记录
        new_record = {
            "id": new_id,
            "userId": user_id,
            "materialId": material_id,
            "borrowDate": borrow_date.isoformat(),
            "returnDate": None,
            "status": status
        }
        return jsonify(new_record), 201

    except mysql.connector.Error as err:
        if conn:
            conn.rollback() # 插入失败时回滚
        return jsonify({"error": str(err)}), 500
    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=8081, debug=True)