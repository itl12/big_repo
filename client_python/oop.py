class A:
    def __init__(self, name):
        print(f"Initializing class A {name}")

class B(A):
    def __init__(self):
        print("Initializing class B")

class C(A):
    def __init__(self, *args):
        print(f"Initializing class C {args}")
        super().__init__('from C')

class D(B, C):
    def __init__(self):
        print("Initializing class D")
        super().__init__()

# Creating an instance of class D
print(D.__mro__)
obj = D()
