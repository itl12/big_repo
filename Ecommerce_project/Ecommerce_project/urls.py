
from django.contrib import admin
from django.urls import path, include 
from . import views 
from django.conf import settings
from django.contrib.staticfiles.urls import static 

urlpatterns = [
    path('admin/', admin.site.urls),
    path('', views.home, name= 'home'),
    path('accounts/', include('App_Login.urls')),
    path('shop/', include('App_Shop.urls')),
    path('cart/', include('App_Cart.urls')),
    path('payment/', include('App_Payment.urls')),
    
] + static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
