# Installation Guide

Follow these steps to set up the project on your machine.

## 1. Install Dependencies

Ensure that you have OpenJDK 17 installed. You can do this by running the following command:

```bash
sudo apt install openjdk-17-jdk
```

## 2. Clone the Repository

Clone the project repository to your local machine:

```bash
git clone https://github.com/aeny0x/mallow.git
```

## 3. Navigate to the Project Directory

Change to the `mallow` directory:

```bash
cd mallow
```

## 4. Set Execute Permissions

Grant execute permissions to the necessary scripts:

```bash
chmod +x build.sh
chmod +x install.sh
```

## 5. Build the Project

Compile the project by running the build script:

```bash
./build
```

## 6. Install the Project

Finally, install the project using the install script:

```bash
./install
```


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
