# Lazy

Utility to postpone initialisation of a value

[![listeners](https://img.shields.io/maven-central/v/ru.noties/lazy.svg?label=lazy)](http://search.maven.org/#search|ga|1|g%3A%22ru.noties%22%20AND%20a%3A%22lazy%22)

```gradle
implementation 'ru.noties:lazy:1.1.0'
```

---

```java
final Lazy<String> lazy = Lazy.of(() -> "I'm so lazy!");
```

```java
final Lazy<Calendar> lazy = Lazy.of(Calendar::getInstance);
final Calendar calendar = lazy.get();
```

```java
final Lazy<Cursor> lazy = Lazy.ofSynchronized(this::query);
```

```java
final Lazy<Integer> lazy = Lazy.of(() -> 42);
final Lazy<Integer> sync = Lazy.ofSynchronized(lazy);
```

## Hide

Starting with `1.1.0` Lazy provides a way to _hide_ wrapped type:

```java
final CharSequence cs = Lazy.ofHidden(CharSequence.class, () -> "Yeah, it's me");

// or

final CharSequence cs = Lazy.of(() -> "We are the lazy!").hide(CharSequence.class);
```

**NB** This works _ONLY_ for interface types as internally `java.lang.reflect.Proxy` is used.

---

With some lifecycle notification (using [lifebus](https://github.com/noties/Lifebus)):

```java
public class MainActivity extends Activity {
    
    private Lifebus<ActivityEvent> lifebus;
    
    private CharSequence hiddenLazy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        lifebus = ActivityLifebus.create(this);
        
        hiddenLazy = Lazy.of(() -> "Really resource consuming thing here")
                .accept(lazy -> lifebus.on(ActivityEvent.DESTROY, () -> {
                    if (lazy.hasValue()) {
                        // release it here
                        lazy.get();
                    }
                }))
                .hide(CharSequence.class);
    }
}
```

```
  Copyright 2018 Dimitry Ivanov (mail@dimitryivanov.ru)

  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
```