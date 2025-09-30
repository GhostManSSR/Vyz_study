gcc -fPIC -shared -g -O0 -o libmylist.so mylist.c

gcc -g -O0 -o test_list test_list.c -L. -lmylist

export LD_LIBRARY_PATH=$(pwd):$LD_LIBRARY_PATH

LD_LIBRARY_PATH=$(pwd) gdb ./test_list

backtrace (или bt) — показать стек вызовов функций (важно для рекурсии).

step - шаг с заходом в саму функцию

next - шаг с обходом