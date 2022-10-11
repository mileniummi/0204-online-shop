
## Authorization
**REQUEST**:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP VERB: POST
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/authentication</span>

BODY:
&nbsp;&nbsp;&nbsp;&nbsp;UserLoginDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;404: user not found
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;403: invalid password


**REQUEST**:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP VERB: POST
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">authentication/register</span>

BODY:
&nbsp;&nbsp;&nbsp;&nbsp;CreateUserDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;404: user not found
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;403: invalid password


## Orders
**REQUEST**:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;HTTP VERB: GET
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/orders</span>

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;401: unauthorized

**REQUEST**:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP VERB: GET
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/orders/{order_id}</span>

HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp;Authorization: Bearer access_token

PARAMETERS:
&nbsp;&nbsp;&nbsp;&nbsp;order_id: UUID

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;401: unauthorized

**REQUEST**:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP VERB: POST
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/orders</span>

HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp;Authorization: Bearer access_token

BODY:
&nbsp;&nbsp;&nbsp;&nbsp;CreateOrderDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;401: unauthorized
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;400: bad request

**REQUEST**:
HTTP VERB: PUT
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/orders/{order_id}</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Authorization: Bearer access_token
PARAMS
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; order_id: UUID
BODY:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;UpdateOrderDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;401: unauthorized
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;400: bad request

**REQUEST**:
HTTP VERB: DELETE
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/order/{order_id}</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
PARAM:
&nbsp;&nbsp;&nbsp;&nbsp;order_id: UUID

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp; HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 404: order with this id does not exists

**REQUEST**:
HTTP VERB: POST
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/order/{order_id}/delivery</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
PARAM:
&nbsp;&nbsp;&nbsp;&nbsp;order_id: UUID

BODY:
&nbsp;&nbsp;&nbsp;&nbsp; CreateOrderDeliveryDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp; HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 404: order with this id does not exists

**REQUEST**:
HTTP VERB: PUT
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/order/{order_id}/delivery</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
PARAM:
&nbsp;&nbsp;&nbsp;&nbsp;order_id: UUID

BODY:
&nbsp;&nbsp;&nbsp;&nbsp;UpdateOrderDeliveryDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp; HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 404: order with this id does not exists
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 403: can’t change order delivery in this state


**REQUEST**:
HTTP VERB: DELETE
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/order/{order_id}/delivery</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
PARAM:
&nbsp;&nbsp;&nbsp;&nbsp;order_id: UUID

BODY: empty

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp; HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 404: order with this id does not exists

**REQUEST**:
HTTP VERB: GET
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/order/{order_id}/payment</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
PARAM:
&nbsp;&nbsp;&nbsp;&nbsp;order_id: UUID

BODY: empty

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp; HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;200: success
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;404: order with this id does not exists



## Shop Items
**REQUEST**:
HTTP VERB: POST
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/shop/items</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
BODY:
&nbsp;&nbsp;&nbsp;&nbsp;GetAllItemsDto

RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp;HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp; 200: success

**REQUEST**:
HTTP VERB: GET
&nbsp;&nbsp;&nbsp;&nbsp;URL: <span style="color:blue">/shop/items/{item_id}</span>
HEADERS:
&nbsp;&nbsp;&nbsp;&nbsp; Authorization: Bearer access_token
PARAMS:
&nbsp;&nbsp;&nbsp;&nbsp;   item_id
RESPONSE:
&nbsp;&nbsp;&nbsp;&nbsp; HTTP CODES:
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp; 200: success
&nbsp;&nbsp;&nbsp;&nbsp;BODY FORMAT: empty


## DTO


PageableDto {
&nbsp;&nbsp;&nbsp;&nbsp;sort: string, // название по какому параметру &nbsp;&nbsp;&nbsp;&nbsp;сортировать, asc или desc
&nbsp;&nbsp;&nbsp;&nbsp;page: number,
&nbsp;&nbsp;&nbsp;&nbsp;size: number
}

Item{
&nbsp;&nbsp;&nbsp;&nbsp;name: string;
&nbsp;&nbsp;&nbsp;&nbsp;price: Money;
}

FiltersDto {
&nbsp;&nbsp;&nbsp;&nbsp;name*: string;
&nbsp;&nbsp;&nbsp;&nbsp;brand: string;
&nbsp;&nbsp;&nbsp;&nbsp;minPrice: Money;
&nbsp;&nbsp;&nbsp;&nbsp;maxPrice: Money;
&nbsp;&nbsp;&nbsp;&nbsp;count: int;
}

GetAllItemsDto extends Pageable {
&nbsp;&nbsp;&nbsp;&nbsp;totalCount: number
&nbsp;&nbsp;&nbsp;&nbsp;filters: FiltersDto
}

UserLoginDto {
&nbsp;&nbsp;&nbsp;&nbsp;username*: string;
&nbsp;&nbsp;&nbsp;&nbsp;password*: string;
}

CreateUserDto extends UseLoginDto {
&nbsp;&nbsp;&nbsp;&nbsp;email*: string;
&nbsp;&nbsp;&nbsp;&nbsp;firstName*: string;
&nbsp;&nbsp;&nbsp;&nbsp;lastName*: string;
&nbsp;&nbsp;&nbsp;&nbsp;phone*: string
}

CreateOrderDto {
&nbsp;&nbsp;&nbsp;&nbsp;items*: Item [ ]
}




UpdateOrderDto {
&nbsp;&nbsp;&nbsp;&nbsp;items*: Item [ ]
}

CreateOrderDeliveryDto{
&nbsp;&nbsp;&nbsp;&nbsp;address*: string;
&nbsp;&nbsp;&nbsp;&nbsp;date*: Date;
}

UpdateOrderDeliveryDto{
&nbsp;&nbsp;&nbsp;&nbsp;address: string;
&nbsp;&nbsp;&nbsp;&nbsp;date: Date;
}



