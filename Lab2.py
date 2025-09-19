import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.model_selection import StratifiedKFold
from sklearn.metrics import accuracy_score
from imblearn.under_sampling import RandomUnderSampler
import numpy as np


# Функции ядер
def kernel_quartic(r):
    return np.where(r <= 1, (1 - r**2)**2, 0)

def kernel_triangular(r):
    return np.where(r <= 1, 1 - r, 0)

def kernel_rectangular(r):
    return np.where(r <= 1, 1, 0)

def select_kernel(Nc):
    Nq = ((Nc * 6 + 13) % 8) % 3 + 1
    if Nq == 1:
        return kernel_quartic
    elif Nq == 2:
        return kernel_triangular
    else:
        return kernel_rectangular

def euclidean_distance(a, b):
    return np.sqrt(np.sum((a - b)**2, axis=1))

def parzen_relative_window(X_train, y_train, X_test, kernel_func):
    y_pred = []
    for x in X_test:
        distances = euclidean_distance(X_train, x)
        h = distances.max()
        weights = kernel_func(distances / h)
        class_scores = {}
        for cls in np.unique(y_train):
            class_scores[cls] = weights[y_train == cls].sum()
        y_pred.append(max(class_scores, key=class_scores.get))
    return np.array(y_pred)

# Загрузка данных
df = pd.read_csv('data1.csv', sep=';')
target_col = 'Class'
Nc = 1

kernel_func = select_kernel(Nc)

results = []

for iteration in range(10):
    # Разбиение на 3 равные части с разным random_state для каждое разбиения
    part1, temp = train_test_split(df, test_size=2/3, stratify=df[target_col], random_state=iteration)
    part2, part3 = train_test_split(temp, test_size=0.5, stratify=temp[target_col], random_state=iteration)

    train = pd.concat([part2, part3]).reset_index(drop=True)
    test = part1.reset_index(drop=True)

    X_train = train.drop(columns=[target_col]).values
    y_train = train[target_col].values
    X_test = test.drop(columns=[target_col]).values
    y_test = test[target_col].values

    y_pred = parzen_relative_window(X_train, y_train, X_test, kernel_func)
    acc = accuracy_score(y_test, y_pred)

    results.append({'Split': iteration + 1, 'Relative_window_accuracy': acc})

results_df = pd.DataFrame(results)
print(results_df)


# Анализ баланса классов в обучающей выборке
train_class_counts = train[target_col].value_counts()
print("Распределение классов в обучающей выборке (до балансировки):")
print(train_class_counts)
print("\nДоля каждого класса в обучающей выборке:")
print(train_class_counts / train_class_counts.sum())

# Балансировка обучающей выборки (undersampling)
rus = RandomUnderSampler(random_state=42)
X_train_balanced, y_train_balanced = rus.fit_resample(train.drop(columns=[target_col]).values, train[target_col])

# Проверяем баланс после балансировки
unique, counts = np.unique(y_train_balanced, return_counts=True)
print("Распределение классов в обучающей выборке (после балансировки):")
print(dict(zip(unique, counts)))

# Далее используем X_train_balanced и y_train_balanced для обучения модели

# Объединяем данные для 10-кратного разбиения и тестирования парзеновского окна

kernel_func = select_kernel(Nc)

X = df.drop(columns=[target_col]).values
y = df[target_col].values

skf = StratifiedKFold(n_splits=10, shuffle=True, random_state=42)

results = []

for i, (train_idx, test_idx) in enumerate(skf.split(X, y), 1):
    X_train_full, X_test = X[train_idx], X[test_idx]
    y_train_full, y_test = y[train_idx], y[test_idx]

    # Балансируем обучающую выборку на каждом разбиении
    X_train_bal, y_train_bal = rus.fit_resample(X_train_full, y_train_full)

    y_pred = parzen_relative_window(X_train_bal, y_train_bal, X_test, kernel_func)
    acc = accuracy_score(y_test, y_pred)

    results.append({'Split': i, 'Relative_window_accuracy': acc})

results_df = pd.DataFrame(results)
print(results_df)

# В итоге после балансировки данные разбения стали более точны из этого следует что
# распределение данных выорки пополам даёт более тоные данные

