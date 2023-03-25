import requests as rq
import random

# def gen_users():
#     for i in range(1000):
#         rq.post("http://localhost:8888/users", json={"name": str(i), "password": str(i)})

descs = ["Varya", "Sanya", "Kate", "Mihas", "Kolyas"]

def gen_products():
    for i in range(1000):
        data = {
            "name": f"{i}",
            "description": f"{random.choice(descs)} shalom",
            "price": i,
            "amount": 300_000_000,
        }
        headers = {
            "Authorization": "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoic3RyaW5nIiwiaWF0IjoxNjc5NzgwNjkxLCJleHAiOjE2Nzk3ODE1OTEsInJvbGVzIjpbXX0.FI1LXNXgZoEb_i1BrAIcZxRSrJNbK0xfOOjtN-vUl12kY5xhHszT8CiW-8bacXTb13xitaNhK068PxYhbtHKiA"
        }
        res = rq.post("http://localhost:8888/_internal/catalogItem", json=data, headers=headers)
        print(i, res)


if __name__ == '__main__':
    gen_products()