import numpy as np
from sklearn.model_selection import KFold
import random
from Imahakil import IMAHAKIL

class DE:
    def __init__(self, learner, X, y, fitness_func,num_folds=5,
                 pop_size=40,crossover_prob=0.3,differential_weight=0.7):
        self.learner = learner
        self.X = X
        self.y = y
        self.differential_weight=differential_weight
        self.num_folds = num_folds
        self.indices = [(train_ind, val_ind) for (train_ind, val_ind) in KFold(n_splits=self.num_folds,shuffle=True,random_state=2).split(X, y)]
        self.fitness_func = fitness_func
        self.pop_size = pop_size
        self.params = []
        self.crossover_prob=crossover_prob

    def add_param(self, param):
        self.params.append(param)
        return self

    def _initialize_population(self):
        return [[param.sample() for param in self.params] for _ in range(self.pop_size)]

    def _to_param_dict(self, ind_params):
        param_dict = {}
        for i in range(len(self.params)):
            param_dict[self.params[i].name] = ind_params[i]
        return param_dict

    def _evaluate_individual(self, ind_params):
        param_dict = self._to_param_dict(ind_params)
        learner_obj = self.learner(**param_dict)

        score = 0
        for train_ind, val_ind in self.indices:
            x_train, y_train = IMAHAKIL().fit_sample(self.X[train_ind, :], self.y[train_ind])
            learner_obj.fit(x_train,y_train)
            score += self.fitness_func(learner_obj.predict(self.X[val_ind, :]), self.y[val_ind])

        return score / self.num_folds

    def _evaluate_population(self):
        return [self._evaluate_individual(ind) for ind in self.population]


    def evolve(self):
        self.population = self._initialize_population()
        best = self.population[0]
        lives = 1  # Number of generations
        while lives > 0:
            lives -= 1
            tmp = []
            for i in range(len(self.population)):
                old = self.population[i]
                index = random.sample(range(len(self.population)), 3)
                x, y, z = self.population[index[0]], self.population[index[1]], self.population[index[2]]
                new = old.copy()
                for j in range(len(new)):
                    if random.random() < self.crossover_prob:
                        new[j]=self.params[j].DEmute(x[j],y[j],z[j],self.differential_weight,new[j])
                new, is_ = self.better(new, old)  # 选择操作
                tmp.append(new)
                t, is_ = self.betterBest(new, best)
                if is_:
                    best = t
                    lives += 1
            self.population = tmp
            print("--------")
            print("score:", self._evaluate_individual(best))
            print("Best individual:", self._to_param_dict(best))
        self._to_param_dict(best), self._evaluate_individual(best)

    # fitness Function
    def better(self,new,old):
        score_new=self._evaluate_individual(new)
        score_old=self._evaluate_individual(old)
        if score_new > score_old:
            return new, True
        else:
            return old, False

    def betterBest(self,new,old):
        score_new = self._evaluate_individual(new)
        score_old = self._evaluate_individual(old)
        if score_new > score_old:
            print("score:", score_new)
            return new, True
        else:
            return old, False