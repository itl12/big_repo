from django.urls import path 
from . import views 

app_name = 'App_Shop'


urlpatterns = [
    path('product_detail/<int:pk>/', views.product_details.as_view(), name='product_detail')
]