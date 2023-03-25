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
            "Authorization": "Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0eXBlIjoiYWNjZXNzIiwic3ViIjoic3RyaW5nIiwiaWF0IjoxNjc5NzY0NjI5LCJleHAiOjE2Nzk3NjU1MjksInJvbGVzIjpbXX0.Qiw9ckjYJnnlT68Bicb_Lg2bZhQq3NGq0aqN1SkU7pbfSr40zd15Dkzs5bT8Fp5clkEoqCNiX2rJOb31IQ_S3g"
        }
        res = rq.post("http://localhost:8888/_internal/catalogItem", json=data, headers=headers)
        print(i, res)


if __name__ == '__main__':
    gen_products()