// 最简单的测试函数
export default function handler(req, res) {
    res.status(200).json({
        success: true,
        message: "测试成功！",
        timestamp: new Date().toISOString()
    });
}