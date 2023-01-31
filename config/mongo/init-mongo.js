db.createUser(
    {
        user: "root",
        pwd: "root",
        roles: [
            {
                role: "readWrite",
                db: "marketplace_positions"
            },
            {
                role: "dbAdmin",
                db: "marketplace_positions"
            }
        ]
    }
)