from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_squared_error
from sklearn import datasets
from sklearn import svm
from geneticHyperopt.param import ContinuousParam, CategoricalParam, ConstantParam
from geneticHyperopt.genetic import GeneticHyperopt


boston_dataset = datasets.load_boston()
X, y = boston_dataset.data, boston_dataset.target

optimizer = GeneticHyperopt(RandomForestRegressor, X, y, mean_squared_error, maximize=False)

n_estimators_param = ContinuousParam("n_estimators", min_limit=5, max_limit=100, is_int=True)
max_depth_param = ContinuousParam("max_depth",min_limit=3, max_limit=20, is_int=True)
min_impurity_param = ContinuousParam("min_impurity_decrease", min_limit=0, is_int=False)
criterion_param = CategoricalParam("criterion", ["mse", "friedman_mse"], [0.6, 0.4])
max_features_param = CategoricalParam("max_features", ["auto", "sqrt", "log2"])
random_state_param = ConstantParam("random_state", 0)
n_jobs_param = ConstantParam("n_jobs", -1)

optimizer.add_param(random_state_param).add_param(n_jobs_param)
optimizer.add_param(max_features_param).add_param(criterion_param)
optimizer.add_param(n_estimators_param).add_param(max_depth_param).add_param(min_impurity_param)

best_params, best_score = optimizer.evolve()

