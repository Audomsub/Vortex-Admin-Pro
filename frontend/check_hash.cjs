const bcrypt = require('bcryptjs');
const hash = '$2a$10$os7NnLh0ui/o9rZraYXzG.AlK64C3GjTNWnC5VUoXLyZ7Xh4d3aoq';
const password = 'password123';
bcrypt.compare(password, hash, (err, res) => {
  console.log('password123 matches:', res);
});
