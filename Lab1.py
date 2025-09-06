import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import matplotlib
from sklearn.preprocessing import LabelEncoder

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
matplotlib.rcParams['font.family'] = 'DejaVu Sans'
matplotlib.rcParams['text.usetex'] = False

cat_cols = df.select_dtypes(include=['object', 'category']).columns

top_n = 20

for col in cat_cols:
    top_vals = df[col].value_counts().index[:top_n]
    plt.figure(figsize=(10, 5))
    sns.countplot(data=df, x=col, order=top_vals)
    plt.title(f"Распределение топ-{top_n} категорий '{col}'")
    plt.xticks(rotation=45)
    plt.tight_layout()
    plt.show()

unique_counts = df[cat_cols].nunique()
print("Количество уникальных категорий по признакам:")
print(unique_counts)

# задание 5
# Выделим числовые столбцы
num_cols = df.select_dtypes(include=['number']).columns

# Посчитаем корреляционную матрицу (по умолчанию корреляция Пирсона)
corr_matrix = df[num_cols].corr()

# Построим тепловую карту корреляций
plt.figure(figsize=(12, 8))
sns.heatmap(corr_matrix, annot=True, fmt='.2f', cmap='coolwarm', center=0, square=True)
plt.title('Корреляционная матрица числовых признаков')
plt.show()

x_col, y_col = num_cols[0], num_cols[1]

# Например, для пары признаков 'feature1' и 'feature2'
sns.scatterplot(data=df, x=x_col, y=y_col)
plt.title(f'Диаграмма рассеяния {x_col} vs {y_col}')
plt.show()

# Пример для категориального признака 'category_col' и числового 'numeric_col'
sns.boxplot(data=df, x=x_col, y=y_col)
plt.title('Boxplot числового признака по категориям')
plt.xticks(rotation=45)
plt.show()

# задание 6
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
#Выбросы — это значения в данных, которые значительно отличаются от остальных наблюдений. Проще говоря, это экстремальные точки, которые находятся далеко за пределами общего распределения данных.
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


