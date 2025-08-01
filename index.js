module.exports = (req, res) => {
  res.setHeader('Content-Type', 'application/json');
  res.status(200).json({
    success: true,
    message: 'TodoApp API 工作正常！',
    timestamp: new Date().toISOString(),
    path: req.url
  });
};