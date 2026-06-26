const { Client } = require('pg');
const client = new Client({
  connectionString: 'postgres://postgres.boskcvexjyixxyhxczpe:Diwooo1661@@aws-1-ap-northeast-1.pooler.supabase.com:5432/postgres'
});
client.connect()
  .then(() => client.query("SELECT id, username, deleted_at FROM users WHERE lower(username) = 'admin'"))
  .then(res => {
    console.log(res.rows);
    client.end();
  })
  .catch(err => {
    console.error(err);
    client.end();
  });
