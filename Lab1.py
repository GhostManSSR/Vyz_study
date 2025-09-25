import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt


file_path = "/content/fitness_and_workout_dataset.csv"

df = pd.read_csv(file_path)

# 1 задание
# Display first 5-10 rows
print("First 10")
print(df.head(10))

# General info about columns, types, non-null count
print("Info: ")
print(df.info())

# Statistical summary of numeric columns
print("Statistical: ")
print(df.describe())

# Dataset size (rows, columns)
print("size: ")
print(df.shape)


# 2 задание

# Количество пропусков по столбцам
missing_counts = df.isnull().sum()

# Доля пропусков в процентах по столбцам
missing_ratio = 100 * missing_counts / len(df)

print("Количество пропусков по столбцам: ", missing_counts)

print("Доля пропусков в процентах по столбцам: ", missing_ratio)

plt.figure(figsize=(12, 6))
sns.heatmap(df.isnull(), cbar=False, yticklabels=False)
plt.title("Матрица пропусков")
plt.show()

# 3 задание

num_cols = df.select_dtypes(include=['number']).columns

df[num_cols].hist(bins=30, figsize=(15, 10))
plt.suptitle("Гистограммы числовых признаков")
plt.show()

plt.figure(figsize=(15, 8))
sns.boxplot(data=df[num_cols], orient='h')
plt.title("Диаграммы размаха (boxplot) числовых признаков")
plt.show()

desc = df[num_cols].describe().T
desc['median'] = df[num_cols].median()
desc['skew'] = df[num_cols].skew()

#skew - Асимметрия поможет количественно оценить скошенность распределения (ближе к 0 — симметрия, >0— скошенность вправо, <0 – влево)
#mean (Среднее) — арифметическое среднее всех значений признака. Это мера центральной тенденции, точка баланса данных. Среднее может быть чувствительно к выбросам, так как учитывает все значения.
#std (Стандартное отклонение) — мера разброса вокруг среднего значения. Выражается в тех же единицах, что и данные. Чем больше std, тем более вариабельны данные. При нормальном распределении примерно 68% значений лежат в диапазоне от (mean - std) до (mean + std)

stats = desc[['mean', 'median', 'std', 'skew']]
print(stats)

# задание 4

import matplotlib.pyplot as plt
import seaborn as sns

def shorten_labels(labels, max_len=5):
    return [label[:max_len] + '...' if len(label) > max_len else label for label in labels]

top_n = 20
cat_cols = df.select_dtypes(include=['object', 'category']).columns

for col in cat_cols:
    top_vals = df[col].value_counts().index[:top_n]
    plt.figure(figsize=(12, 6))
    ax = sns.countplot(data=df, x=col, order=top_vals)
    plt.title(f"Распределение топ-{top_n} категорий '{col}'")
    # Ограничиваем длину подписей
    ax.set_xticklabels(shorten_labels(top_vals))
    plt.xticks(rotation=45, ha='right')  # Поворачиваем подписи для читаемости
    plt.tight_layout()
    plt.show()

unique_counts = df[cat_cols].nunique()
print("Количество уникальных категорий по признакам:")
print(unique_counts)


# задание 5

import seaborn as sns
import matplotlib.pyplot as plt

# Клонируем исходный датафрейм
df_encoded = df.copy()

# Преобразуем все строковые и категориальные столбцы в числовые коды
for col in df_encoded.select_dtypes(include=['object', 'category']).columns:
    df_encoded[col] = df_encoded[col].astype('category').cat.codes

# Выделяем все числовые столбцы
num_cols = df_encoded.select_dtypes(include=['number']).columns

# Считаем корреляционную матрицу
corr_matrix = df_encoded[num_cols].corr()

# Строим тепловую карту корреляций
plt.figure(figsize=(12, 8))
sns.heatmap(corr_matrix, annot=True, fmt='.2f', cmap='coolwarm', center=0, square=True)
plt.title('Корреляционная матрица по числовым и закодированным категориальным признакам')
plt.show()

# Построим диаграмму рассеяния для первых двух числовых признаков
x_col, y_col = num_cols[0], num_cols[1]
sns.scatterplot(data=df_encoded, x=x_col, y=y_col)
plt.title(f'Диаграмма рассеяния {x_col} vs {y_col}')
plt.show()

# Построение boxplot числового признака по категориям из исходных данных (если есть)
cat_cols = df.select_dtypes(include=['object', 'category']).columns
if len(cat_cols) > 0:
    sns.boxplot(data=df, x=cat_cols[0], y=num_cols[0])
    plt.title(f'Boxplot признака {num_cols[0]} по категориям {cat_cols[0]}')
    plt.xticks(rotation=45)
    plt.show()


# задание 6

import pandas as pd
from sklearn.preprocessing import LabelEncoder

# 1. Нормализация названий столбцов
df.columns = df.columns.str.lower().str.strip().str.replace(' ', '_')

# 2. Обработка пропусков

num_cols = df.select_dtypes(include=['number']).columns
cat_cols = df.select_dtypes(include=['object', 'category']).columns

# Заполнение пропусков медианой (без inplace, через .loc)
for col in num_cols:
    median_val = df[col].median()
    df.loc[:, col] = df[col].fillna(median_val)

# Заполнение пропусков модой (без inplace)
for col in cat_cols:
    mode_val = df[col].mode()[0]
    df.loc[:, col] = df[col].fillna(mode_val)

# 3. Преобразование категориальных признаков в числовой формат

le = LabelEncoder()
for col in cat_cols:
    df.loc[:, col] = le.fit_transform(df[col])

# Проверка результата
print(df.info())
print(df.head())
print("Пропуски после обработки:\n", df.isnull().sum())


# задание 7
import seaborn as sns
import matplotlib.pyplot as plt

# Выделим числовые признаки
num_cols = df.select_dtypes(include=['number']).columns

# Посчитаем выбросы по методу IQR для каждого числового столбца
outliers_counts = {}

for col in num_cols:
    Q1 = df[col].quantile(0.25)
    Q3 = df[col].quantile(0.75)
    IQR = Q3 - Q1
    lower_bound = Q1 - 1.5 * IQR
    upper_bound = Q3 + 1.5 * IQR
    outliers = df[(df[col] < lower_bound) | (df[col] > upper_bound)]
    outliers_counts[col] = len(outliers)

# Определим признак, где число выбросов максимально
target_col = max(outliers_counts, key=outliers_counts.get)
print(f"Признак с максимальным числом выбросов: {target_col}, количество выбросов: {outliers_counts[target_col]}")

# Визуализация выбросов до обработки
plt.figure(figsize=(8, 4))
sns.boxplot(x=df[target_col])
plt.title(f'Boxplot до обработки выбросов: {target_col}')
plt.show()

# Обработка выбросов - обрезка по IQR
Q1 = df[target_col].quantile(0.25)
Q3 = df[target_col].quantile(0.75)
IQR = Q3 - Q1
lower_bound = Q1 - 1.5 * IQR
upper_bound = Q3 + 1.5 * IQR

df[f'{target_col}_clipped'] = df[target_col].clip(lower=lower_bound, upper=upper_bound)

# Визуализация после обработки
plt.figure(figsize=(8, 4))
sns.boxplot(x=df[f'{target_col}_clipped'])
plt.title(f'Boxplot после обрезки выбросов: {target_col}')
plt.show()
