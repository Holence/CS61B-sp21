Iterable的继承完全可以这样，但gradescope不给过

```Java
public interface Deque<T> extends Iterable<T>{}

public class ArrayDeque<T> implements Deque<T>{}
public class LinkedListDeque<T> implements Deque<T>{}
```

`.equals()`中可以用新语法`obj instanceof Deque alias`，但gradescope不给过

而且应该用iterator去遍历，因为LinkedListDeque的`get()`是低效的，而LinkedListDeque的iterator实现了高效的模式。但gradescope就是编译不了，只能写成for循环`get()`了。

```Java
public boolean equals(Object obj) {
    if (this == obj){
        return true;
    }
    if (obj instanceof Deque alias) {
        if (size() != alias.size()){
            return false;
        }
        Iterator<T> a = iterator();
        Iterator<T> b = alias.iterator();
        while (a.hasNext()) {
            if (!a.next().equals(b.next())){
                return false;
            }
        }
        return true;
    }
    return false;
}
```
