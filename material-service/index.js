const express = require('express');
const mysql = require('mysql2/promise'); // 使用 mysql2/promise
const app = express();
const port = 8082;

// 1. 中间件
app.use(express.json());

// 2. 数据库连接池
// (使用你 application.properties 中的凭据)
const dbConfig = {
    host: 'localhost',
    port: 3306,
    user: 'root',
    password: 'abcABC369258',
    database: 'soa_lab1'
};

const pool = mysql.createPool(dbConfig);

// 3. 根路径 (保留)
app.get('/', (req, res) => {
    res.send('Hello from Node.js Material Service (Port 8082) - Now connected to Java DB');
});

// 4. API: 获取所有物资 (实现)
app.get('/materials', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM materials');
        res.json(rows);
    } catch (error) {
        res.status(500).send(error.message);
    }
});

// 5. API: 获取单个物资 (实现)
app.get('/materials/:id', async (req, res) => {
    try {
        const [rows] = await pool.query('SELECT * FROM materials WHERE id = ?', [req.params.id]);
        if (rows.length === 0) {
            res.status(404).send('Material not found');
        } else {
            res.json(rows[0]);
        }
    } catch (error) {
        res.status(500).send(error.message);
    }
});

// 6. API: 减少库存 (核心实现)
// 供 Java 网关调用
app.put('/materials/:id/decrement', async (req, res) => {
    const id = req.params.id;
    try {
        // 使用 WHERE quantity > 0 确保操作的原子性，防止超卖
        const [result] = await pool.query(
            'UPDATE materials SET quantity = quantity - 1 WHERE id = ? AND quantity > 0',
            [id]
        );

        if (result.affectedRows === 0) {
            // 如果没有行被更新, 检查是 "未找到" 还是 "库存不足"
            const [rows] = await pool.query('SELECT * FROM materials WHERE id = ?', [id]);
            if (rows.length === 0) {
                res.status(404).send('Material not found');
            } else {
                res.status(400).send('Material out of stock');
            }
        } else {
            res.status(200).send('Stock decremented');
        }
    } catch (error) {
        res.status(500).send(error.message);
    }
});

// 7. API: 增加库存 (用于补偿/回滚)
// 供 Java 网关调用
app.put('/materials/:id/increment', async (req, res) => {
    try {
        await pool.query('UPDATE materials SET quantity = quantity + 1 WHERE id = ?', [req.params.id]);
        res.status(200).send('Stock incremented (compensation)');
    } catch (error) {
        res.status(500).send(error.message);
    }
});

app.listen(port, () => {
    console.log(`Node.js Material service listening at http://localhost:${port}`);
});