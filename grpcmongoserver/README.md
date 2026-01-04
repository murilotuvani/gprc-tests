
```bash 
docker exec -it mongodb /bin/bash
mongosh -u mongoadmin -p secret
```

```sql
// 1. Mude para o contexto do banco 'grpc'
use grpc

// 2. Crie o usuário com a role 'dbAdmin' e 'readWrite'
db.createUser({
  user: "grpc",
  pwd: "grpc",
  roles: [
    { role: "readWrite", db: "grpc" },
    { role: "dbAdmin", db: "grpc" }
  ]
})
```