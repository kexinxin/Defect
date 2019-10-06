from sklearn.neighbors import NearestNeighbors
import random
import numpy as np

class Smote:
    """
    Implement SMOTE, synthetic minority oversampling technique.

    Parameters
    -----------
    sample      2D (numpy)array
                 class samples

    N           Integer
                amount of SMOTE N%

    k           Integer
                number of nearest neighbors k
                k <= number of minority class samples

    Attributes
    ----------
    newIndex    Integer
                keep a count of number of synthetic samples
                initialize as 0

    synthetic   2D array
                array for synthetic samples

    neighbors   K-Nearest Neighbors model

    """
    def __init__(self,data,label,b, k):
        self.data=data
        self.label=label
        self.sample = self.getminSample(data,label)
        self.k = k
        self.T = len(self.sample)
        self.ms, self.ml, self.minority = self.calculate_degree()
        self.b=b
        self.newIndex = 0
        self.synthetic = []
        self.neighbors = NearestNeighbors(n_neighbors=self.k).fit(self.sample)

    def getminSample(self,data,label):
        sample=[]
        for i in range(len(data)):
            if label[i]==1:
                sample.append(data[i])
        sample=np.array(sample)
        return sample

    def calculate_degree(self):
        pos, neg = 0, 0
        for i in range(0, len(self.label)):
            if self.label[i] == 1:
                pos += 1
            elif self.label[i] == 0:
                neg += 1
        ml = max(pos, neg)
        ms = min(pos, neg)
        if pos > neg:
            minority = 0
        else:
            minority = 1
        return ms, ml, minority


    def over_sampling(self):
        # if self.N < 100:
        #     self.T = int((self.N / 100) * self.T)
        #     self.N = 100
        # self.N = int(self.N / 100)

        self.N=int((self.ml - self.ms) * self.b)
        for i in range(0,self.N):
        #for i in range(0, self.T):
            j=random.randint(0,self.T-1)
            nn_array = self.compute_k_nearest(j)
            self.populate(1, j, nn_array)


        data_new=np.array(self.synthetic)
        label_new = np.ones(len(data_new))
        return np.append(self.data, data_new, axis=0), np.append(self.label, label_new, axis=0)

    def compute_k_nearest(self, i):
        nn_array = self.neighbors.kneighbors([self.sample[i]], self.k, return_distance=False)
        if len(nn_array) is 1:
            return nn_array[0]
        else:
            return []

    def populate(self, N, i, nn_array):
        while N is not 0:
            nn = random.randint(0, self.k - 1)
            self.synthetic.append([])
            for attr in range(0, len(self.sample[i])):
                dif = self.sample[nn_array[nn]][attr] - self.sample[i][attr]
                gap = random.random()
                self.synthetic[self.newIndex].append(self.sample[i][attr] + gap * dif)
            self.newIndex += 1
            N -= 1
