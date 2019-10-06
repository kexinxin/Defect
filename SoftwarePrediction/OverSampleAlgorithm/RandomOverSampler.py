import random

import numpy as np

class RandomOverSampler:
    def __init__(self,data,label,N):
        self.data=data
        self.label=label
        self.N=N
        self.sample = self.getminSample(data, label)
        self.T = len(self.sample)
        self.synthetic=[]

    def getminSample(self,data,label):
        sample=[]
        for i in range(len(data)):
            if label[i]==1:
                sample.append(data[i])
        sample=np.array(sample)
        return sample

    def samplping(self):
        count = int((self.N / 100) * self.T)
        for i in range(0, count):
            index=random.randint(0,self.T-1)
            self.synthetic.append(self.sample[index])
        data_new = np.array(self.synthetic)
        label_new = np.ones(len(data_new))
        return np.append(self.data, data_new, axis=0), np.append(self.label, label_new, axis=0)
