import random
from sklearn.neighbors import NearestNeighbors
import numpy as np

class BorderlineOverSampling:
    """
    RandomOverSampler

    Parameters
    -----------
    X           2D array
                feature space X

    Y           array
                label, y is either 0 or 1

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
        self.N=1
        self.ms, self.ml, self.minority = self.calculate_degree()
        self.b = b
        self.neighbors = NearestNeighbors(n_neighbors=self.K).fit(self.X)
        self.synthetic = []
        self.sample = self.getminSample(X, Y)
        self.minNeighbors = NearestNeighbors(n_neighbors=self.K).fit(self.sample)
        self.newIndex = 0

    def getminSample(self,data,label):
        sample=[]
        for i in range(len(data)):
            if label[i]==1:
                sample.append(data[i])
        sample=np.array(sample)
        return sample

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
        #G=142

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
        indexs=[]
        for i in range(0,len(r)):
            if r[i]>=0.5 and r[i]<0.9:
                indexs.append(i)

        for i in range(0,int(G)):
            index = random.randint(0,len(indexs)-1)
            nn_array = self.compute_k_nearest(indexs[index])
            self.populate(self.N, indexs[index], nn_array)

        data_new = np.array(self.synthetic)
        label_new = np.ones(len(data_new))
        return np.append(self.X, data_new, axis=0), np.append(self.Y, label_new, axis=0)

    def compute_k_nearest(self, i):
        nn_array = self.minNeighbors.kneighbors([self.sample[i]], self.K, return_distance=False)
        if len(nn_array) is 1:
            return nn_array[0]
        else:
            return []

    def populate(self, N, i, nn_array):
        while N is not 0:
            nn = random.randint(0, self.K - 1)
            self.synthetic.append([])
            for attr in range(0, len(self.sample[i])):
                dif = self.sample[nn_array[nn]][attr] - self.sample[i][attr]
                gap = random.random()
                self.synthetic[self.newIndex].append(self.sample[i][attr] + gap * dif)
            self.newIndex += 1
            N -= 1