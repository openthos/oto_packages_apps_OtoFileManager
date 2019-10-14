#
# Copyright (C) 2013 The Android Open Source Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

LOCAL_PATH := $(call my-dir)

#
# Build app code.
#
include $(CLEAR_VARS)

LOCAL_MODULE_TAGS := optional

LOCAL_STATIC_JAVA_LIBRARIES := android-support-v4
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-appcompat
LOCAL_STATIC_JAVA_LIBRARIES += android-support-v7-recyclerview

LOCAL_SRC_FILES := $(call all-java-files-under, src) \
    $(call all-renderscript-files-under, src) \
    $(call all-subdir-Java-files) \
    ../OtoCloudService/src/org/openthos/seafile/ISeafileService.aidl

ifeq ($(shell test $(PLATFORM_SDK_VERSION) -eq 22 && echo Lollipop), Lollipop)
    LOCAL_SRC_FILES += $(call all-java-files-under, platform-22/src)
else ifeq ($(shell test $(PLATFORM_SDK_VERSION) -eq 27 && echo Oreo), Oreo)
    LOCAL_SRC_FILES += $(call all-java-files-under, platform-27/src)
else
    LOCAL_SRC_FILES += $(call all-java-files-under, platform/src)
endif

LOCAL_RESOURCE_DIR := $(LOCAL_PATH)/res
LOCAL_AAPT_FLAGS := --auto-add-overlay

LOCAL_PROTOC_OPTIMIZE_TYPE := nano

#LOCAL_PROTOC_FLAGS := --proto_path=$(LOCAL_PATH)/protos/

#LOCAL_SDK_VERSION := current

LOCAL_PACKAGE_NAME := OtoFileManager
#LOCAL_CERTIFICATE := shared
LOCAL_CERTIFICATE := platform
LOCAL_PRIVILEGED_MODULE := true

LOCAL_PROGUARD_FLAG_FILES := proguard.flags

LOCAL_JNI_SHARED_LIBRARIES := libp7zip

include $(BUILD_PACKAGE)

include $(CLEAR_VARS)
LOCAL_MODULE := libp7zip
LOCAL_SRC_FILES := libs/x86_64/libp7zip.so
LOCAL_MODULE_TAGS := optional
LOCAL_MODULE_CLASS := SHARED_LIBRARIES
LOCAL_MODULE_SUFFIX := .so
include $(BUILD_PREBUILT)
