<template>
  <div class="launch">
    <div class="banner">
      <Logo alt="RMAC logo" :config="{ maxHeight: '10rem' }" name="rmac-logo-combined" animated />
      <h1><pre>C o n s o l e</pre></h1>
    </div>
    <hr />
    <div class="input">
      <label for="rmac-server-url">
        Server URL
        <Info class="infocon">The RMAC Server URL through which all your hosts register.</Info>
      </label>
      <input type="text" name="rmac-server-url" placeholder="my-rmac-server.com" required />
    </div>
  </div>
</template>

<script setup>
import Info from '@/components/Common/Info.vue';
import Logo from '@/components/Common/Logo.vue';
</script>

<style scoped>
.launch {
  display: flex;
  justify-content: center;
  flex-direction: column;
  height: 100vh;
  text-align: center;
}
.launch .banner {
}
.launch hr {
  margin: 1rem 30vw 1.5rem 30vw;
  border: none;
  border-style: solid;
  border-color: darkgray;
  border-width: 1px;
  border-top: none;
}

.launch h1 pre {
  font-family: Arial, Helvetica, sans-serif;
  color: rgb(120 120 120);
}
.launch .input {
}

.input label {
  display: block;
}
.input input {
  width: 60vw;
  text-align: center;
  font-size: 1.1rem;
  padding: 0.3rem 1rem;
  border-radius: 999px;
  border: 1px solid grey;
  margin-top: 0.5rem;
  box-shadow: none;
  transition: box-shadow var(--theme-transition-duration) linear,
    border var(--theme-transition-duration) linear;
}

.input input:focus {
  border: 0px solid grey;
  box-shadow: 0px 2px 6px 0px grey;
}

.input input:focus {
  outline: grey;
}
.infocon {
  display: inline-block;
  vertical-align: middle;
  margin-left: 0.5rem;
}

@media (min-width: 768px) {
}

@media (min-width: 1024px) {
}
</style>
