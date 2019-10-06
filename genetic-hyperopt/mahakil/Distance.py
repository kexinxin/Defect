class Distance:
    def __init__(self,distance,label):
        self.__distance__ = distance
        self.__label__= label

    def get_distance(self):
        return self.__distance__

    def get_label(self):
        return self.__label__
    def __str__(self):
        return str("distance:{} label:{} ".format(self.__distance__,self.__label__))
