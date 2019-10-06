class ConfusionMatrix:
    def __init__(self, tn, fp, fn, tp):
        self.__tn__ = tn
        self.__fp__ = fp
        self.__fn__= fn
        self.__tp__= tp

    def get_tn(self):
        return self.__tn__

    def get_fp(self):
        return self.__fp__

    def get_fn(self):
        return self.__fn__

    def get_tp(self):
        return self.__tp__

    def __str__(self):
        return str("{};{};{};{}".format(self.__tn__, self.__fp__,self.__fn__,self.__tp__))