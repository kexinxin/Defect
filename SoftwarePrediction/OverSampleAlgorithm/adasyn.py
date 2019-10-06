from sklearn.neighbors import NearestNeighbors
import numpy as np
import random


class Adasyn:
    """
    ADASYN: Adaptive Synthetic Sampling

    Parameters
    -----------
    X           2D array
                feature space X

    Y           array
                label, y is either 0 or 1

    dth         float in (0,1]
                preset threshold
                maximum tolerated degree of class imbalance ratio

    b           float in [0, 1]
                desired balance level after generation of the synthetic data

    K           Integer
                number of nearest neighbors

    Attributes
    ----------
    ms          Integer
                the number of minority class examples

    ml          Integer
                the number of majority class examples

    d           float in n (0, 1]
                degree of class imbalance, d = ms/ml

    minority    Integer label
                the class label which belong to minority

    neighbors   K-Nearest Neighbors model

    synthetic   2D array
                array for synthetic samples
    """

    def __init__(self, X, Y, b, K):
        self.X = X
        self.Y = Y
        self.K = K
        self.ms, self.ml, self.minority = self.calculate_degree()
        self.b = b
        self.neighbors = NearestNeighbors(n_neighbors=self.K).fit(self.X)
        self.synthetic = []

    def calculate_degree(self):
        pos, neg = 0, 0
        for i in range(0, len(self.Y)):
            if self.Y[i] == 1:
                pos += 1
            elif self.Y[i] == 0:
                neg += 1
        ml = max(pos, neg)
        ms = min(pos, neg)
        if pos > neg:
            minority = 0
        else:
            minority = 1
        return ms, ml, minority

    def sampling(self):
        # a: calculate the number of synthetic data examples
        #    that need to be generated for the minority class
        G = (self.ml - self.ms) * self.b
        #G=185
        # b: for each xi in minority class, find K nearest neighbors
        # based on Euclidean distance in n-d space and calculate ratio
        # ri = number of examples in K nearest neighbors of xi that
        # belong to majority class, therefore ri in [0,1]
        r = []
        for i in range(0, len(self.Y)):
            if self.Y[i] == self.minority:
                delta = 0
                neighbors = self.neighbors.kneighbors([self.X[i]], self.K, return_distance=False)[0]
                for neighbors_index in neighbors:
                    if self.Y[neighbors_index] != self.minority:
                        delta += 1
                r.append(1. * delta / self.K)

        # c: normalize ri to get density distribution
        r = np.array(r)
        sum_r = np.sum(r)
        if sum_r == 0:
            raise ValueError("NaN values appear. Please "
                             "try to use SMOTE or other methods.""")
        r = r / sum_r

        # d: calculate the number of synthetic data examples that
        # need to be generated for each minority example xi
        g = r * G
        totalNumber=0
        for index in range(0,self.ms):
            g[index]=int(g[index])
            totalNumber+=g[index]

        extraGene=G-totalNumber
        if extraGene<self.ms:
            for index in range(0,int(extraGene)):
                g[index]=g[index]+1

        # totalNumber = 0
        # for index in range(0,self.ms):
        #     g[index]=int(g[index])
        #     totalNumber+=g[index]
        #
        # print(totalNumber)

        # e: for each minority class data example, generate gi
        # synthetic data examples

        index = 0
        for i in range(0, len(self.Y)):
            if self.Y[i] == self.minority:
                neighbors = self.neighbors.kneighbors([self.X[i]], self.K, return_distance=False)[0]
                xzi_set = []
                for j in neighbors:
                    if self.Y[j] == self.minority:
                        xzi_set.append(j)

                for g_index in range(0, int(g[index])):
                    if len(xzi_set)==0:
                        self.synthetic.append(xi.tolist())
                    else:
                        random_num = random.randint(0, len(xzi_set) - 1)
                        xzi = np.array(self.X[xzi_set[random_num]])
                        xi = np.array(self.X[i])
                        random_lambda = random.random()
                        self.synthetic.append((xi + (xzi - xi) * random_lambda).tolist())
                index += 1
        data_new = np.array(self.synthetic)
        label_new = np.ones(len(data_new))
        return np.append(self.X, data_new, axis=0), np.append(self.Y, label_new, axis=0)