const express = require('express');
const axios = require('axios'); // 引入 axios
const app = express();
const port = 8082;

// 1. 定义 Java 数据服务的根 URL
const JAVA_API_URL = 'http://localhost:8080';

// 2. 中间件
app.use(express.json());

// 3. (保留) 根路径
app.get('/', (req, res) => {
    res.send('Hello from Node.js Material Service (Port 8082) - Now connected to Java DB');
});

// 4. API: 获取所有物资 (代理 Java 服务)
app.get('/materials', async (req, res) => {
    try {
        // 跨语言调用：GET 到 Java 的 /materials
        const response = await axios.get(`${JAVA_API_URL}/materials`);
        res.json(response.data); // 将 Java 返回的数据转发给调用者
    } catch (error) {
        res.status(500).send(error.message);
    }
});

// 5. API: 获取单个物资 (代理 Java 服务)
app.get('/materials/:id', async (req, res) => {
    try {
        const response = await axios.get(`${JAVA_API_URL}/materials/${req.params.id}`);
        res.json(response.data);
    } catch (error) {
        if (error.response && error.response.status === 404) {
            res.status(404).send('Material not found');
        } else {
            res.status(500).send(error.message);
        }
    }
});

// 6. API: 减少库存 (代理 Java 服务)
// 这是给 Java 网关调用的
app.put('/materials/:id/decrement', async (req, res) => {
    try {
        // 跨语言调用：PUT 到 Java 的 /materials/{id}/decrement
        const response = await axios.put(`${JAVA_API_URL}/materials/${req.params.id}/decrement`);
        res.status(200).json(response.data);
    } catch (error) {
        // 转发 Java 服务返回的具体错误 (例如 "out of stock")
        if (error.response) {
            res.status(error.response.status).send(error.response.data);
        } else {
            res.status(500).send(error.message);
        }
    }
});

app.listen(port, () => {
    console.log(`Node.js Material service listening at http://localhost:${port}`);
});