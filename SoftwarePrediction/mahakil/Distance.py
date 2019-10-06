class Distance:
    def __init__(self,distance,data,label):
        self.__distance__ = distance
        self.__label__= label
        self.__data__=data

    def get_distance(self):
        return self.__distance__

    def get_label(self):
        return self.__label__

    def get_data(self):
        return self.__data__

    def __str__(self):
        return str("distance:{} label:{} data:{}".format(self.__distance__,self.__label__,self.__data__))
