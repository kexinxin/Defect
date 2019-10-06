class MetricsName:
    def __init__(self, name, value):
        self.__name__ = name
        self.__value__ = value

    def get_name(self):
        return self.__name__

    def get_value(self):
        return self.__value__

    def __str__(self):
        return str("name:{} value:{} ".format(self.__name__,self.__value__))

def cmp(self, other): # 自定义比较函数
    if self.value < other.value:
        return 1
    elif self.value == other.value:
        return 0
    else:
        return -1