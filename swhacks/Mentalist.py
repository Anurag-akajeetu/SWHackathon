from __future__ import print_function
from keras.models import Sequential
from keras.layers.core import Dense, Dropout, Activation
from keras.models import model_from_json
from sklearn.cross_validation import StratifiedKFold
import os
import sys
import pymysql
import numpy as np
import pandas as pd
from scipy.fftpack import fft
import matplotlib.pyplot as plt

np.random.seed(1234)


class Mentalist:
    def __init__(self, msg):
        self.file = "patrikjane"
        self.n_folds = 5
        self.batch_size = 1
        self.nb_classes = 4
        self.nb_epoch = 10
        self.message = msg
        self.hostname = '10.218.110.136'
        self.username = 'anurag'
        self.password = 'anurag'
        self.database = 'SWhacks'
        self.query = "Select * from eegFeaturess where name='ab'"
        self.category = {0: 'c',
                         1: 'e',
                         2: 'i',
                         3: 'm',
                         }
        self.model = self.load_model()
        print(self.message)

    def Transform_input(self):
            data = []
            raw_list = list(map(lambda x: x.rstrip(","), self.message))
            filtered_raw = list(filter(lambda a: a != 'null', raw_list))
            filtered_raw = list(map(int, filtered_raw))
            data.append(filtered_raw)
            df = pd.DataFrame(data)
            X = df.as_matrix()
            X = X.astype('float32')
            X = X / 512
            return X

    def Prediction(self, check):
        prediction = self.model.predict(np.asmatrix(check), verbose=1)
        index = np.argmax(prediction)
        return self.category[index]

    def create_model(self):
        model = Sequential()
        model.add(Dense(256, input_shape=(2561,)))
        model.add(Activation('relu'))
        model.add(Dropout(0.25))
        model.add(Dense(128))
        model.add(Activation('relu'))
        model.add(Dropout(0.30))
        model.add(Dense(64))
        model.add(Activation('relu'))
        model.add(Dropout(0.25))
        model.add(Dense(4))
        model.add(Activation('softmax'))
        model.compile(loss='categorical_crossentropy',
                      optimizer='adam',
                      metrics=['accuracy'])
        return model

    def train_and_evaluate_model(self, model, X, Y, train, test):
        model.fit(X[train],
                  Y[train],
                  batch_size=self.batch_size,
                  nb_epoch=self.nb_epoch,
                  verbose=1)
        score = model.evaluate(X[test],
                               Y[test],
                               verbose=0)
        print('Test accuracy:', score[1])
        return model, score[1]

    def load_model(self):
        try:
            json_file = open("C:/Users/Anurag/swhacks/patrikjane.json", 'r')
            loaded_model_json = json_file.read()
            json_file.close()
            loaded_model = model_from_json(loaded_model_json)
            loaded_model.load_weights( "C:/Users/Anurag/swhacks/patrikjane.h5")
        except:
            loaded_model = None
        return loaded_model

    def save_model(self, model):
        model_json = model.to_json()
        with open("C:/Users/Anurag/swhacks/patrikjane.json", "w") as json_file:
            json_file.write(model_json)
        model.save_weights("C:/Users/Anurag/swhacks/patrikjane.h5")

    def retrain(self):
            Connection = pymysql.connect(host=self.hostname,
                                         user=self.username,
                                         passwd=self.password,
                                         db=self.database)
            df = self.EEG_fetch(Connection, self.query)
            Connection.close()
            X, Y, skf = self.prepare_data(df)
            tmodel = []
            tscore = []
            for i, (train, test) in enumerate(skf):
                print("Running Fold", i + 1, "/", self.n_folds)
                model = self.create_model()
                models, scores = self.train_and_evaluate_model(model, X, Y, train, test)
                tmodel.append(models)
                tscore.append(scores)
            return tmodel[np.argmax(tscore)], np.max(tscore)

    def EEG_fetch(self, conn, query):
        cur = conn.cursor()
        cur.execute(query)
        count = 1
        data = []
        for name, category, raw in cur.fetchall():
            raw_list = raw.decode("utf-8")[1:-1].split(", ")
            filtered_raw = list(filter(lambda a: a != 'null', raw_list))
            filtered_raw = list(map(int, filtered_raw))
            filtered_raw.append(category)
            data.append(filtered_raw)
            count += 1
        df = pd.DataFrame(data)
        return df

    def EEG_delete(self, conn, query):
        cur = conn.cursor()
        cur.execute(query)
        cur = None

    def FFT(self, df):
        df = df.apply(lambda x: fft(x), axis=1)
        return df

    def Scale(self, df, absolute):
        N = 2561
        if absolute:
            df = df.apply(lambda x: 2.0 / N * np.abs(x))
        else:
            df = df.apply(lambda x: 2.0 / N * (x))
        return df

    def prepare_data(self, df):
        X = df[df.columns[:-1]].as_matrix()
        X = X.astype('float32')/512
        Y_ = df[df.columns[-1]].as_matrix()
        Y = pd.get_dummies(Y_).as_matrix()
        skf = self.stratified(Y_)
        return X, Y, skf

    def stratified(self, Y_):
        skf = StratifiedKFold(Y_,
                              n_folds=self.n_folds,
                              shuffle=True)
        return skf

if __name__ == '__main__':
    if len(sys.argv) > 0:
        param = sys.argv[1:]
        print(param)
        mental = Mentalist(param)
        if param[0] != 'Retrain':
            Check = mental.Transform_input()
            output = mental.Prediction(Check)
            print(output)
        else:
            print("Retraining")
            new_model, scored = mental.retrain()
            mental.save_model(new_model)
            mental.model = new_model
            print("New Accuracy :", scored)
    else:
        print("Please pass the input")
        sys.exit(1)


