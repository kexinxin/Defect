import numpy as np
from sklearn.metrics import f1_score,accuracy_score,precision_score
from geneticHyperopt.param import ContinuousParam, CategoricalParam, ConstantParam
from geneticHyperopt.DE import DE
from sklearn.ensemble import RandomForestClassifier
from sklearn import svm

dataset = np.loadtxt('I:\\tools\\genetic-hyperopt\\dataset\\kc2.txt', delimiter=",")
length = len(dataset[0])
X = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
y[y>=1]=1

#optimizer = DE(RandomForestClassifier, X, y, f1_score)
# criterion_param=CategoricalParam("criterion",["gini","entropy"])
# max_features_param=CategoricalParam("max_features",["auto","sqrt","log2",None])
# min_samples_leaf_param=ContinuousParam("min_samples_leaf",min_limit=1, max_limit=10, is_int=True)
# n_estimators_param=ContinuousParam("n_estimators",min_limit=5,max_limit=20,is_int=True)
# bootstrap_param=CategoricalParam("bootstrap",["True","False"])
# optimizer.add_param(criterion_param).add_param(max_features_param).add_param(min_samples_leaf_param).add_param(n_estimators_param).add_param(bootstrap_param)
# best_params, best_score = optimizer.evolve()

optimizer = DE(svm.SVC, X, y, accuracy_score)
C_param=ContinuousParam("C",min_limit=1,max_limit=2000,is_int=True)
kernel_param=ConstantParam("kernel","rbf")
gamma_param=ContinuousParam("gamma",min_limit=0.000001,max_limit=8,is_int=False)
optimizer.add_param(C_param).add_param(kernel_param).add_param(gamma_param)
optimizer.evolve()
print("++++")