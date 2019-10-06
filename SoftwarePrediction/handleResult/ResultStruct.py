class ResultStruct:
    def __init__(self,accList,precisionList,recallList,f1List,gMeanList,matrixList,avgAcc,avgPrecision,avgRecall,avgF1,avgGmean):
        self.__accList__ = accList
        self.__precisionList__ = precisionList
        self.__recallList__= recallList
        self.__f1List__= f1List
        self.__gMeanList__=gMeanList
        self.__matrixList__=matrixList
        self.__avgAcc__=avgAcc
        self.__avgPrecision__=avgPrecision
        self.__avgRecall__=avgRecall
        self.__avgF1__=avgF1
        self.__avgGmean__=avgGmean

    # def get_tn(self):
    #     return self.__matrixList__[0]
    #
    # def get_fp(self):
    #     return self.__matrixList__[1]
    #
    # def get_fn(self):
    #     return self.__matrixList__[2]
    #
    # def get_tp(self):
    #     return self.__matrixList__[3]

    def get_accList(self):
        return self.__accList__

    def get_precisionList(self):
        return self.__precisionList__

    def get_recallList(self):
        return self.__recallList__

    def getF1List(self):
        return self.__f1List__

    def getGmeanList(self):
        return self.__gMeanList__

    def getMatrixList(self):
        return self.__matrixList__

    def getAvgAcc(self):
        return self.__avgAcc__

    def getAvgPrecision(self):
        return self.__avgPrecision__

    def getAvgRecall(self):
        return self.__avgRecall__

    def getAvgF1(self):
        return self.__avgF1__

    def getAvgGeam(self):
        return self.__avgGmean__

    def __str__(self):
        return str("avgAcc {};avgPrecision {};avgRecall {};avgF1 {};avgGmean {}".format(self.__avgAcc__, self.__avgPrecision__,self.__avgRecall__,self.__avgF1__,self.__avgGmean__))