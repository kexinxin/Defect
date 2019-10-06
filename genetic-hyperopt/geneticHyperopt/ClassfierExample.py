import numpy as np
from sklearn import svm
from sklearn.metrics import f1_score,accuracy_score,precision_score
from geneticHyperopt.param import ContinuousParam, CategoricalParam, ConstantParam
from geneticHyperopt.genetic import GeneticHyperopt


dataset = np.loadtxt('I:\\tools\\genetic-hyperopt\\dataset\\kc2.txt', delimiter=",")
length = len(dataset[0])
X = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
y[y>=1]=1

optimizer = GeneticHyperopt(svm.SVC, X, y, accuracy_score, maximize=True)
C_param=ContinuousParam("C",min_limit=1,max_limit=2000,is_int=True)
kernel_param=ConstantParam("kernel","rbf")
gamma_param=ContinuousParam("gamma",min_limit=0.000001,max_limit=8,is_int=False)
optimizer.add_param(C_param).add_param(kernel_param).add_param(gamma_param)
best_params, best_score = optimizer.evolve()

