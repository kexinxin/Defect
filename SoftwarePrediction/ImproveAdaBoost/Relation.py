class Relation:
    def __init__(self, minNumber, majorNumber,distance):
        self.__minNumber__ = minNumber
        self.__majorNumber__ = majorNumber
        self.__distance__=distance

    def get_minNumber(self):
        return self.__minNumber__

    def get_majorNumber(self):
        return self.__majorNumber__

    def get_distance(self):
        return self.__distance__

    def __str__(self):
        return str("minNumber:{} majorNumber:{} distance:{}".format(self.__minNumber__, self.__majorNumber__,self.__distance__))