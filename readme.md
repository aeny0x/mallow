# Installation 

Dependencies: sudo apt install openjdk-17-jdk

git clone https://github.com/aeny0x/mallow.git
cd mallow
chmod +x build.sh
chmod +x install.sh
./build
./install

# Code Snippets

```
define even? as
    lambda x . (x mod 2) = 0
end
```

```
define odd? as
    lambda x . (x mod 2) ~= 0
end
```

```
define range as
    lambda low . lambda high .
        if (low > high) then nil else
            pair low ((range | (low + 1)) | high)
end
```

```
define sum as
    lambda x . lambda y . x + y
end
```

```
define product as
    lambda x . lambda y . x * y
end
```
