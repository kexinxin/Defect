import numpy as np
from sklearn.metrics import f1_score,accuracy_score,precision_score
from geneticHyperopt.param import ContinuousParam, CategoricalParam, ConstantParam
from geneticHyperopt.genetic import GeneticHyperopt
from sklearn.ensemble import RandomForestClassifier

dataset = np.loadtxt('I:\\tools\\genetic-hyperopt\\dataset\\kc2.txt', delimiter=",")
length = len(dataset[0])
X = dataset[:, 0:length - 1]
y = dataset[:, length - 1]
y[y>=1]=1

#clf2 = RandomForestClassifier(n_estimators=10,max_features=math.sqrt(n_features), max_depth=None,min_samples_split=2, bootstrap=True)
optimizer = GeneticHyperopt(RandomForestClassifier, X, y, f1_score, maximize=True,classfierParameterSize=5)

criterion_param=CategoricalParam("criterion",["gini","entropy"])
max_features_param=CategoricalParam("max_features",["auto","sqrt","log2",None])
min_samples_leaf_param=ContinuousParam("min_samples_leaf",min_limit=1, max_limit=10, is_int=True)
n_estimators_param=ContinuousParam("n_estimators",min_limit=5,max_limit=20,is_int=True)
bootstrap_param=CategoricalParam("bootstrap",["True","False"])

generation_param=ContinuousParam("generation",min_limit=0,max_limit=10,is_int=True)
rate_param=ContinuousParam("rate",min_limit=0.3,max_limit=0.7)
pfp_param=ContinuousParam("pfp",min_limit=0.3,max_limit=0.7)

optimizer.add_param(criterion_param).add_param(max_features_param).add_param(min_samples_leaf_param).add_param(n_estimators_param).add_param(bootstrap_param)
#optimizer.add_param()
optimizer.add_param(generation_param).add_param(rate_param).add_param(pfp_param)

best_params, best_score = optimizer.evolve()



