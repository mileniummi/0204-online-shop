import requests as rq
import random

def gen_users():
    for i in range(1000):
        rq.post("http://localhost:8888/users", json={"name": str(i), "password": str(i)})

descs = ["Varya", "Sanya", "Kate", "Mihas", "Kolyas"]

def gen_products():
    for i in range(1000):
        data = {
            "name": f"{i}",
            "description": f"{random.choice(descs)} shalom",
            "price": i,
            "amount": 300_000,
        }
        headers = {
            "Authorization": "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoiMSIsImlhdCI6MTY3ODkwODI5NCwiZXhwIjoxNjc4OTA5MTk0LCJyb2xlcyI6W119.BI8or9LcAWCBKIeA7ufiZMtYlev6ONMmEpwg0aZ8dPy69J5dUD__UlS_vTwlHSraMCk7g2c1zcA0IVCzPCzowQ"
        }
        res = rq.post("http://localhost:8888/_internal/catalogItem", json=data, headers=headers)
        print(i, res)


if __name__ == '__main__':
    gen_products()