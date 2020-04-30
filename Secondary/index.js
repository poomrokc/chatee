const express = require('express');
const mongoose = require('mongoose');
const bodyParser = require('body-parser');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const app = express();

const saltRounds = 12;
const jwtSecret = 'qwer4wdk4!09%C';

function getRandomInt(max) {
  return Math.floor(Math.random() * Math.floor(max));
}

app.use(bodyParser.json());

mongoose.connect('mongodb://localhost:27017/chat', {useNewUrlParser: true, useUnifiedTopology: true});

var UserModel = require('./models/User');
var UserGroupModel = require('./models/UserGroup');
var GroupModel = require('./models/Group');
var ChatModel = require('./models/Chat');


const jwtMiddleWare = async(req, res, next) => {
	try {
		let auth = req.header('Authorization').split(' ')[1];
		var decoded = jwt.verify(auth, jwtSecret);
		let user = await UserModel.findOne({username:decoded.username}, '-password');
		req.user = user;
		next();
	}
	catch(err) {
		res.status(401).send('Unauthorized');
	}
};

app.use((req,res,next)=>{
	res.set('Active-Server', 'Secondary');
	next();
});

app.use('/images', express.static('images'));

app.get('/', (req, res) => {
	res.json('Welcome to chat backend');
});

app.post('/register', async(req, res) => {
	try {
		let hashed = await bcrypt.hash(req.body.password, saltRounds);
		let photo = 'https://hueco.ml/chat/images/'+getRandomInt(8)+'.jpg';
		let insert = {
			username: req.body.username,
			password: hashed,
			name: req.body.name,
			photo
		};
		let exist = await UserModel.findOne({username:req.body.username});
		if(exist)
			return res.status(400).send('Username already exist');
		await UserModel.create(insert);
		let token = jwt.sign({username:req.body.username}, jwtSecret);
		res.json({suceess:true,token});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/login', async(req, res) => {
	try {
		let exist = await UserModel.findOne({username:req.body.username});
		if(!exist)
			return res.status(404).send('Username not found');
		let result = await bcrypt.compare(req.body.password, exist.password);
		if(!result)
			return res.status(400).send('Password does not match');
		let token = jwt.sign({username:req.body.username}, jwtSecret);
		res.json({suceess:true,token});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.get('/profile', jwtMiddleWare, async(req, res) => {
	try {
		res.json(req.user);
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/createGroup', jwtMiddleWare, async(req, res) => {
	try {
		let photo = 'https://hueco.ml/chat/images/'+getRandomInt(8)+'.jpg';
		let group = await GroupModel.create({name: req.body.name,photo});
		await UserGroupModel.create({user: req.user._id, group: group._id});
		res.json({success:true});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/joinGroup', jwtMiddleWare, async(req, res) => {
	try {
		let group = await GroupModel.findOne({_id: mongoose.Types.ObjectId(req.body.groupID)});
		if(!group)
			return res.status(404).send('Group not found');
		let exist = await UserGroupModel.findOne({user: req.user._id, group: group._id});
		if(exist)
			return res.status(400).send('You already in the group');
		await UserGroupModel.create({user: req.user._id, group: group._id});
		res.json({success:true});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/leaveGroup', jwtMiddleWare, async(req, res) => {
	try {
		let group = await GroupModel.findOne({_id: mongoose.Types.ObjectId(req.body.groupID)});
		if(!group)
			return res.status(404).send('Group not found');
		await UserGroupModel.deleteOne({user: req.user._id, group: group._id});
		res.json({success:true});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/chat', jwtMiddleWare, async(req, res) => {
	try {
		let group = await GroupModel.findOne({_id: mongoose.Types.ObjectId(req.body.groupID)});
		if(!group)
			return res.status(404).send('Group not found');
		let exist = await UserGroupModel.findOne({user: req.user._id, group: group._id});
		if(!exist)
			return res.status(401).send('You are not in the group');
		await ChatModel.create({group: group._id, user: req.user._id, message:req.body.message});
		res.json({success:true});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.get('/mygroups', jwtMiddleWare, async(req, res) => {
	try {
		let groups = await UserGroupModel.find({user:req.user._id});
		let data = [];
		for(let i=0;i<groups.length;i++) {
			let g = {members:[]};
			let group = await GroupModel.findOne({_id:groups[i].group});
			g.name = group.name;
			g._id = group._id;
			g.photo = group.photo;
			let memb = await UserGroupModel.find({group:group._id});
			for(let m=0;m<memb.length;m++) {
				let member = await UserModel.findOne({_id:memb[m].user});
				g.members.push({_id:member._id,photo:member.photo,name:member.name});
			}
			data.push(g);
		}
		res.json({sucess:true,data});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.get('/mychat', jwtMiddleWare, async(req, res) => {
	try {
		let groupID = mongoose.Types.ObjectId(req.query.groupID);
		let timeStamp = new Date(req.query.timeStamp);
		let exist = await UserGroupModel.findOne({group:groupID});
		if(!exist)
			return res.status(401).send('You are not in the group');
		let chats = await ChatModel.find({group:groupID,created:{$gt:timeStamp}}).sort({created:1});
		res.json({success:true,data:chats});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/dump', async(req, res) => {
	try {
		if(req.body.password !== 'ParallelMiniProject')
			return res.status(401).send('Unauthorized');
		var users = await UserModel.find({},'-__v');
		var groups = await GroupModel.find({},'-__v');
		var chats = await ChatModel.find({},'-__v');
		var usergroups = await UserGroupModel.find({},'-__v');
		res.json({success:true,data:{users,groups,chats,usergroups}});
	} catch(err) {
		res.status(400).send('Bad Request');
	}
});

app.post('/bregister', async(req, res) => {
	try {
		if(req.body.password!='ParallelMiniProject')
			return res.json({success:false});
		let dat=req.body.data;
		dat._id = mongoose.Types.ObjectId(dat._id);
		await UserModel.create(dat);
		return res.json({success:true});
	} catch(err) {
		return res.json({success:false});
	}
});

app.post('/bgroup', async(req, res) => {
	try {
		if(req.body.password!='ParallelMiniProject')
			return res.json({success:false});
		let dat=req.body.data;
		dat._id = mongoose.Types.ObjectId(dat._id);
		await GroupModel.create(dat);
		return res.json({success:true});
	} catch(err) {
		return res.json({success:false});
	}
});

app.post('/bgu', async(req, res) => {
	try {
		if(req.body.password!='ParallelMiniProject')
			return res.json({success:false});
		let dat=req.body.data;
		dat._id = mongoose.Types.ObjectId(dat._id);
		dat.group = mongoose.Types.ObjectId(dat.group);
		dat.user = mongoose.Types.ObjectId(dat.user);
		await UserGroupModel.create(dat);
		return res.json({success:true});
	} catch(err) {
		return res.json({success:false});
	}
});

app.post('/bchat', async(req, res) => {
	try {
		if(req.body.password!='ParallelMiniProject')
			return res.json({success:false});
		let dat=req.body.data;
		dat._id = mongoose.Types.ObjectId(dat._id);
		dat.group = mongoose.Types.ObjectId(dat.group);
		dat.user = mongoose.Types.ObjectId(dat.user);
		console.log(dat);
		await ChatModel.create(dat);
		return res.json({success:true});
	} catch(err) {
		return res.json({success:false});
	}
});

app.post('/blg', async(req, res) => {
	try {
		if(req.body.password!='ParallelMiniProject')
			return res.json({success:false});
		let dat=req.body.data;
		dat.group = mongoose.Types.ObjectId(dat.group);
		dat.user = mongoose.Types.ObjectId(dat.user);
		await UserGroupModel.deleteOne(dat);
		return res.json({success:true});
	} catch(err) {
		return res.json({success:false});
	}
});

app.listen(9998, () => {
	console.log('Start secondary server at port 9998.');
});